package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.response.BloqueMicroRetoResponseDTO;
import com.adaptcode.utp.dto.response.CodigoMicroRetoResponseDTO;
import com.adaptcode.utp.dto.response.ContenidoMicroRetoResponseDTO;
import com.adaptcode.utp.dto.response.FragmentoMicroRetoResponseDTO;
import com.adaptcode.utp.dto.response.MicroRetoDetalleResponseDTO;
import com.adaptcode.utp.dto.response.MicroRetoResumenResponseDTO;
import com.adaptcode.utp.dto.response.OpcionMicroRetoResponseDTO;
import com.adaptcode.utp.dto.response.OrdenarBloquesContenidoDTO;
import com.adaptcode.utp.dto.response.PredecirSalidaContenidoDTO;
import com.adaptcode.utp.dto.response.SeleccionMultipleContenidoDTO;
import com.adaptcode.utp.dto.response.SeleccionarFragmentoContenidoDTO;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.Alternativa;
import com.adaptcode.utp.model.Bloque;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.repository.CursoRepository;
import com.adaptcode.utp.repository.MicroRetoRepository;
import com.adaptcode.utp.repository.TemaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MicroRetoServiceImp implements MicroRetoService {

	private final CursoRepository cursoRepository;
	private final TemaRepository temaRepository;
	private final MicroRetoRepository microRetoRepository;

	@Override
	@Transactional(readOnly = true)
	public List<MicroRetoResumenResponseDTO> obtenerResumenes(
			Long cursoId, Long temaId, NivelMicroReto nivel) {
		validarIdentificador(cursoId);
		if (!cursoRepository.existsByIdAndActivoTrue(cursoId)) {
			throw new ResourceNotFoundException("El curso no existe.");
		}

		if (temaId != null) {
			validarIdentificador(temaId);
			Tema tema = temaRepository.findByIdAndActivoTrue(temaId)
					.orElseThrow(() -> new ResourceNotFoundException("El tema no existe."));
			if (!cursoId.equals(tema.getCurso().getId())) {
				throw new SolicitudInvalidaException(
						"El tema no pertenece al curso indicado.");
			}
		}

		return buscarMicroRetos(cursoId, temaId, nivel).stream()
				.map(this::aResumen)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public MicroRetoDetalleResponseDTO obtenerDetalle(Long id) {
		validarIdentificador(id);
		MicroReto microReto = microRetoRepository
				.findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("El micro-reto no existe."));

		return new MicroRetoDetalleResponseDTO(
				microReto.getId(), microReto.getTema().getId(), microReto.getTipo(), microReto.getNivel(),
				microReto.getEnunciado(), crearContenido(microReto));
	}

	private List<MicroReto> buscarMicroRetos(Long cursoId, Long temaId, NivelMicroReto nivel) {
		if (temaId == null && nivel == null) {
			return microRetoRepository.findAllByTema_Curso_IdAndTema_ActivoTrueAndActivoTrueOrderByEnunciadoAsc(cursoId);
		}
		if (temaId == null) {
			return microRetoRepository
					.findAllByTema_Curso_IdAndTema_ActivoTrueAndActivoTrueAndNivelOrderByEnunciadoAsc(cursoId, nivel);
		}
		if (nivel == null) {
			return microRetoRepository
				.findAllByTema_Curso_IdAndTema_IdAndTema_ActivoTrueAndActivoTrueOrderByEnunciadoAsc(cursoId, temaId);
		}
		return microRetoRepository
				.findAllByTema_Curso_IdAndTema_IdAndTema_ActivoTrueAndActivoTrueAndNivelOrderByEnunciadoAsc(
						cursoId, temaId, nivel);
	}

	private MicroRetoResumenResponseDTO aResumen(MicroReto microReto) {
		return new MicroRetoResumenResponseDTO(microReto.getId(), microReto.getTema().getId(),
				microReto.getTipo(), microReto.getNivel(), microReto.getEnunciado());
	}

	private ContenidoMicroRetoResponseDTO crearContenido(MicroReto microReto) {
		return switch (microReto.getTipo()) {
			case SELECCION_MULTIPLE -> new SeleccionMultipleContenidoDTO(opcionesTexto(microReto));
			case ORDENAR_BLOQUES -> new OrdenarBloquesContenidoDTO(microReto.getBloques().stream()
					.map(this::aBloqueDTO)
					.toList());
			case PREDECIR_SALIDA -> new PredecirSalidaContenidoDTO(
					new CodigoMicroRetoResponseDTO(microReto.getLenguaje(), microReto.getCodigoBase()),
					opcionesTexto(microReto));
			case SELECCIONAR_FRAGMENTO -> new SeleccionarFragmentoContenidoDTO(microReto.getAlternativas().stream()
					.map(this::aFragmentoDTO)
					.toList());
		};
	}

	private List<OpcionMicroRetoResponseDTO> opcionesTexto(MicroReto microReto) {
		return microReto.getAlternativas().stream()
				.map(alternativa -> new OpcionMicroRetoResponseDTO(alternativa.getId(), alternativa.getTexto()))
				.toList();
	}

	private BloqueMicroRetoResponseDTO aBloqueDTO(Bloque bloque) {
		return new BloqueMicroRetoResponseDTO(bloque.getId(), bloque.getTexto());
	}

	private FragmentoMicroRetoResponseDTO aFragmentoDTO(Alternativa alternativa) {
		return new FragmentoMicroRetoResponseDTO(
				alternativa.getId(), alternativa.getCodigo(), alternativa.getLenguaje());
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}
}
