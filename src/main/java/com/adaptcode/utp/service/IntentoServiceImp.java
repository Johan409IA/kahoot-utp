package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.IntentoRequest;
import com.adaptcode.utp.dto.response.IntentoResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.InsigniaObtenida;
import com.adaptcode.utp.model.Intento;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.ProgresoMicroReto;
import com.adaptcode.utp.model.TipoInsignia;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.InsigniaObtenidaRepository;
import com.adaptcode.utp.repository.IntentoRepository;
import com.adaptcode.utp.repository.MicroRetoRepository;
import com.adaptcode.utp.repository.ProgresoMicroRetoRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntentoServiceImp implements IntentoService {

	private final IntentoRepository intentoRepository;
	private final UsuarioRepository usuarioRepository;
	private final MicroRetoRepository microRetoRepository;
	private final ProgresoMicroRetoRepository progresoMicroRetoRepository;
	private final InsigniaObtenidaRepository insigniaObtenidaRepository;

	@Override
	@Transactional
	public IntentoResponse registrarIntento(Long usuarioId, IntentoRequest request) {
		Usuario usuario = validarUsuario(usuarioId);
		MicroReto microReto = microRetoRepository
				.findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(
						request.microRetoId())
				.orElseThrow(() -> new ResourceNotFoundException("El micro-reto no existe."));
		int siguiente = (int) intentoRepository.countByUsuario_IdAndMicroReto_Id(
				usuarioId, request.microRetoId()) + 1;
		Intento intento = new Intento(usuario, microReto, siguiente,
				request.acierto(), request.usoAyuda());
		intento.setTiempoMs(request.tiempoMs());
		Intento guardado = intentoRepository.save(intento);
		actualizarProgresoAtomico(usuario, microReto, request.acierto(), request.usoAyuda());
		otorgarInsigniasAutomaticas(usuario, guardado);
		return aResponse(guardado);
	}

	@Override
	@Transactional(readOnly = true)
	public IntentoResponse mostrarPorId(Long usuarioId, Long id) {
		validarIdentificador(id);
		validarUsuario(usuarioId);
		Intento intento = intentoRepository.findById(id)
				.filter(i -> usuarioId.equals(i.getUsuario().getId()))
				.orElseThrow(() -> new ResourceNotFoundException("El intento no existe."));
		return aResponse(intento);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IntentoResponse> listarPorUsuarioYReto(Long usuarioId, Long microRetoId) {
		validarUsuario(usuarioId);
		validarIdentificador(microRetoId);
		return intentoRepository.listarPorUsuarioYReto(usuarioId, microRetoId).stream()
				.map(this::aResponse)
				.toList();
	}

	private Usuario validarUsuario(Long usuarioId) {
		validarIdentificador(usuarioId);
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("El usuario no existe."));
		if (!usuario.isActivo()) {
			throw new SolicitudInvalidaException("El usuario no está activo.");
		}
		return usuario;
	}

	private void actualizarProgresoAtomico(
			Usuario usuario, MicroReto microReto, boolean acierto, boolean usoAyuda) {
		if (!acierto) {
			return;
		}
		ProgresoMicroReto progreso = progresoMicroRetoRepository
				.findByUsuario_IdAndMicroReto_Id(usuario.getId(), microReto.getId())
				.orElseGet(() -> new ProgresoMicroReto(usuario, microReto));
		if (!progreso.isCompletado()) {
			progreso.setCompletado(true);
			if (usoAyuda) {
				progreso.setResueltoConAyuda(true);
				progreso.setPuntoDominio(false);
			} else {
				progreso.setResueltoConAyuda(false);
				progreso.setPuntoDominio(true);
			}
			progresoMicroRetoRepository.save(progreso);
		}
	}

	private void otorgarInsigniasAutomaticas(Usuario usuario, Intento intento) {
		if (intento.isAcierto()
				&& progresoMicroRetoRepository.countByUsuario_IdAndCompletadoTrue(
						usuario.getId()) == 1) {
			otorgarSiNoExiste(usuario, TipoInsignia.PRIMER_PASO);
		}
		if (intento.isAcierto() && intento.getNroIntento() >= 3) {
			otorgarSiNoExiste(usuario, TipoInsignia.PERSEVERANTE);
		}
		// TODO(fase-gamificacion): EXPLORADOR, DOMINIO_BASICO y CURSO_COMPLETADO
		// requieren análisis transversal de cursos/temas y se otorgan en
		// ProgressService cuando exista el cálculo de completitud por curso.
	}

	private void otorgarSiNoExiste(Usuario usuario, TipoInsignia tipo) {
		if (!insigniaObtenidaRepository.existsByUsuario_IdAndTipo(
				usuario.getId(), tipo)) {
			insigniaObtenidaRepository.save(new InsigniaObtenida(usuario, tipo));
		}
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private IntentoResponse aResponse(Intento intento) {
		return new IntentoResponse(
				intento.getId(),
				intento.getUsuario().getId(),
				intento.getMicroReto().getId(),
				intento.getNroIntento(),
				intento.isAcierto(),
				intento.isUsoAyuda(),
				intento.getTiempoMs());
	}
}
