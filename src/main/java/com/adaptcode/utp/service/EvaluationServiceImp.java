package com.adaptcode.utp.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.EvaluarIntentoRequest;
import com.adaptcode.utp.dto.request.IntentoRequest;
import com.adaptcode.utp.dto.response.EvaluarIntentoResponse;
import com.adaptcode.utp.dto.response.IntentoResponse;
import com.adaptcode.utp.dto.response.RetroalimentacionExplicadaResponse;
import com.adaptcode.utp.dto.response.SolucionExplicadaResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.Alternativa;
import com.adaptcode.utp.model.Bloque;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.Retroalimentacion;
import com.adaptcode.utp.model.TipoMicroReto;
import com.adaptcode.utp.repository.MicroRetoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImp implements EvaluationService {

	private final MicroRetoRepository microRetoRepository;
	private final IntentoService intentoService;

	@Override
	@Transactional
	public EvaluarIntentoResponse evaluarIntento(
			Long usuarioId, EvaluarIntentoRequest request) {
		validarIdentificador(request.microRetoId());
		MicroReto reto = microRetoRepository
				.findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(
						request.microRetoId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"El micro-reto no existe."));
		boolean acierto = calificar(reto, request);
		RetroalimentacionExplicadaResponse retro = null;
		if (!acierto && request.alternativaId() != null) {
			retro = buscarFeedback(reto, request.alternativaId());
		}
		IntentoResponse registrado = intentoService.registrarIntento(usuarioId,
				new IntentoRequest(reto.getId(), acierto, request.usoAyuda(),
						request.tiempoMs()));
		boolean verSolucion = registradosIntentos(registrado) >= 3;
		SolucionExplicadaResponse solucion = verSolucion
				? construirSolucion(reto) : null;
		return new EvaluarIntentoResponse(registrado.id(),
				registrado.nroIntento(), acierto, retro, verSolucion, solucion);
	}

	private boolean calificar(MicroReto reto, EvaluarIntentoRequest request) {
		if (reto.getTipo() == TipoMicroReto.ORDENAR_BLOQUES) {
			if (request.alternativaId() != null) {
				throw new SolicitudInvalidaException(
						"El reto de ordenamiento se responde con el orden de bloques.");
			}
			if (request.ordenBloques() == null || request.ordenBloques().isEmpty()) {
				throw new SolicitudInvalidaException("El orden de bloques es obligatorio.");
			}
			List<Long> esperado = reto.getBloques().stream()
					.sorted(Comparator.comparingInt(Bloque::getOrdenCorrecto))
					.map(Bloque::getId)
					.toList();
			return esperado.equals(request.ordenBloques());
		}
		if (request.alternativaId() == null) {
			throw new SolicitudInvalidaException("La alternativa es obligatoria.");
		}
		if (request.ordenBloques() != null && !request.ordenBloques().isEmpty()) {
			throw new SolicitudInvalidaException(
					"El reto no se responde con orden de bloques.");
		}
		Alternativa elegida = reto.getAlternativas().stream()
				.filter(a -> request.alternativaId().equals(a.getId()))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException(
						"La alternativa no existe."));
		return elegida.isCorrecta();
	}

	private RetroalimentacionExplicadaResponse buscarFeedback(
			MicroReto reto, Long alternativaId) {
		return reto.getRetroalimentaciones().stream()
				.filter(r -> r.getAlternativa() != null
						&& alternativaId.equals(r.getAlternativa().getId()))
				.findFirst()
				.map(this::aFeedback)
				.orElse(null);
	}

	private SolucionExplicadaResponse construirSolucion(MicroReto reto) {
		if (reto.getTipo() == TipoMicroReto.ORDENAR_BLOQUES) {
			String orden = reto.getBloques().stream()
					.sorted(Comparator.comparingInt(Bloque::getOrdenCorrecto))
					.map(Bloque::getTexto)
					.toList()
					.toString();
			return new SolucionExplicadaResponse(orden,
					"Solución explicada: revisa el orden paso a paso. "
							+ "Al usarla el reto queda resuelto con ayuda y no entrega punto de dominio.");
		}
		String correcta = reto.getAlternativas().stream()
				.filter(Alternativa::isCorrecta)
				.findFirst()
				.map(a -> a.getTexto() != null ? a.getTexto() : a.getCodigo())
				.orElse("Sin respuesta registrada.");
		return new SolucionExplicadaResponse(correcta,
				"Solución explicada: compara tu elección con la respuesta correcta. "
						+ "Al usarla el reto queda resuelto con ayuda y no entrega punto de dominio.");
	}

	private int registradosIntentos(IntentoResponse registrado) {
		return registrado.nroIntento() != null ? registrado.nroIntento() : 0;
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private RetroalimentacionExplicadaResponse aFeedback(Retroalimentacion retro) {
		return new RetroalimentacionExplicadaResponse(
				retro.getCausa(),
				retro.getExplicacion(),
				retro.getSecuenciaTextual(),
				retro.getSugerencia());
	}
}
