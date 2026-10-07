package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.PreguntaDocenteRequest;
import com.adaptcode.utp.dto.response.PreguntaDocenteResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.OrigenPregunta;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.MicroRetoRepository;
import com.adaptcode.utp.repository.PreguntaDocenteRepository;
import com.adaptcode.utp.repository.QuizItemRepository;
import com.adaptcode.utp.repository.TemaRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreguntaDocenteServiceImp implements PreguntaDocenteService {

	private final PreguntaDocenteRepository preguntaDocenteRepository;
	private final UsuarioRepository usuarioRepository;
	private final TemaRepository temaRepository;
	private final MicroRetoRepository microRetoRepository;
	private final QuizItemRepository quizItemRepository;

	@Override
	@Transactional
	public PreguntaDocenteResponse crearPreguntaPropia(Long docenteId, PreguntaDocenteRequest request) {
		Usuario docente = validarDocente(docenteId);
		Tema tema = validarTema(request.temaId());
		PreguntaDocente pregunta = new PreguntaDocente(docente, OrigenPregunta.PROPIA,
				tema, request.tipo(), request.nivel(), request.enunciado());
		return aResponse(preguntaDocenteRepository.save(pregunta));
	}

	@Override
	@Transactional
	public PreguntaDocenteResponse clonarPreguntaInstitucional(Long docenteId, Long microRetoId) {
		Usuario docente = validarDocente(docenteId);
		validarIdentificador(microRetoId);
		MicroReto microReto = microRetoRepository
				.findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(microRetoId)
				.orElseThrow(() -> new ResourceNotFoundException("El micro-reto no existe."));
		PreguntaDocente copia = new PreguntaDocente(docente, OrigenPregunta.COPIA,
				microReto.getTema(), microReto.getTipo(), microReto.getNivel(),
				microReto.getEnunciado());
		copia.marcarOrigenInstitucional(microReto);
		return aResponse(preguntaDocenteRepository.save(copia));
	}

	@Override
	@Transactional(readOnly = true)
	public PreguntaDocenteResponse mostrarPorId(Long docenteId, Long id) {
		validarIdentificador(id);
		return aResponse(buscarPropia(docenteId, id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PreguntaDocenteResponse> listarPorDocente(
			Long docenteId, OrigenPregunta origen) {
		validarDocente(docenteId);
		List<PreguntaDocente> preguntas = origen == null
				? preguntaDocenteRepository
						.findAllByDocente_IdAndActivaTrueOrderByIdAsc(docenteId)
				: preguntaDocenteRepository.buscarPropiasPorDocenteYOrigen(
						docenteId, origen);
		return preguntas.stream()
				.map(this::aResponse)
				.toList();
	}

	@Override
	@Transactional
	public PreguntaDocenteResponse actualizarPregunta(
			Long docenteId, Long id, PreguntaDocenteRequest request) {
		PreguntaDocente pregunta = buscarPropia(docenteId, id);
		if (pregunta.getOrigen() == OrigenPregunta.INSTITUCIONAL) {
			throw new SolicitudInvalidaException(
					"La pregunta institucional no se puede modificar.");
		}
		validarNoUsadaEnQuizListo(id);
		Tema tema = validarTema(request.temaId());
		pregunta.setTema(tema);
		pregunta.setTipo(request.tipo());
		pregunta.setNivel(request.nivel());
		pregunta.setEnunciado(request.enunciado());
		return aResponse(preguntaDocenteRepository.save(pregunta));
	}

	@Override
	@Transactional
	public void eliminarPregunta(Long docenteId, Long id) {
		PreguntaDocente pregunta = buscarPropia(docenteId, id);
		if (pregunta.getOrigen() == OrigenPregunta.INSTITUCIONAL) {
			throw new SolicitudInvalidaException(
					"La pregunta institucional no se puede eliminar.");
		}
		validarNoUsadaEnQuizListo(id);
		pregunta.setActiva(false);
		preguntaDocenteRepository.save(pregunta);
	}

	private void validarNoUsadaEnQuizListo(Long preguntaId) {
		if (quizItemRepository.existsByPregunta_IdAndQuiz_Estado(
				preguntaId, EstadoQuiz.LISTO)) {
			throw new SolicitudInvalidaException(
					"La pregunta está usada en un quiz Listo y ya no se puede modificar.");
		}
	}

	private PreguntaDocente buscarPropia(Long docenteId, Long id) {
		validarDocente(docenteId);
		return preguntaDocenteRepository.findById(id)
				.filter(p -> p.isActiva() && docenteId.equals(p.getDocente().getId()))
				.orElseThrow(() -> new ResourceNotFoundException("La pregunta no existe."));
	}

	private Usuario validarDocente(Long docenteId) {
		validarIdentificador(docenteId);
		Usuario docente = usuarioRepository.findById(docenteId)
				.orElseThrow(() -> new ResourceNotFoundException("El docente no existe."));
		if (docente.getRol() != RolUsuario.DOCENTE_UTP || !docente.isActivo()) {
			throw new SolicitudInvalidaException("El usuario no es un docente activo.");
		}
		return docente;
	}

	private Tema validarTema(Long temaId) {
		validarIdentificador(temaId);
		return temaRepository.findByIdAndActivoTrue(temaId)
				.orElseThrow(() -> new ResourceNotFoundException("El tema no existe."));
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private PreguntaDocenteResponse aResponse(PreguntaDocente pregunta) {
		return new PreguntaDocenteResponse(
				pregunta.getId(),
				pregunta.getDocente().getId(),
				pregunta.getOrigen(),
				pregunta.getMicroRetoOrigen() != null
						? pregunta.getMicroRetoOrigen().getId() : null,
				pregunta.getTema().getId(),
				pregunta.getTipo(),
				pregunta.getNivel(),
				pregunta.getEnunciado(),
				pregunta.isActiva());
	}
}
