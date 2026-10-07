package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.RespuestaSalaRequest;
import com.adaptcode.utp.dto.response.RespuestaSalaResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.EstadoSala;
import com.adaptcode.utp.model.ParticipacionSala;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.RespuestaSala;
import com.adaptcode.utp.model.Sala;
import com.adaptcode.utp.repository.AnulacionPreguntaRepository;
import com.adaptcode.utp.repository.ParticipacionSalaRepository;
import com.adaptcode.utp.repository.PreguntaDocenteRepository;
import com.adaptcode.utp.repository.QuizItemRepository;
import com.adaptcode.utp.repository.RespuestaSalaRepository;
import com.adaptcode.utp.repository.SalaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RespuestaSalaServiceImp implements RespuestaSalaService {

	private final RespuestaSalaRepository respuestaSalaRepository;
	private final ParticipacionSalaRepository participacionSalaRepository;
	private final SalaRepository salaRepository;
	private final QuizItemRepository quizItemRepository;
	private final PreguntaDocenteRepository preguntaDocenteRepository;
	private final AnulacionPreguntaRepository anulacionPreguntaRepository;

	@Override
	@Transactional
	public RespuestaSalaResponse registrar(Long salaId, RespuestaSalaRequest request) {
		Sala sala = buscarSala(salaId);
		if (sala.getEstado() != EstadoSala.EN_CURSO) {
			throw new SolicitudInvalidaException(
					"Solo se aceptan respuestas con la pregunta en curso.");
		}
		ParticipacionSala participacion = participacionSalaRepository
				.findById(request.participacionId())
				.filter(p -> salaId.equals(p.getSala().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La participación no existe."));
		if (participacion.isExpulsado()) {
			throw new SolicitudInvalidaException(
					"La participación fue expulsada de la sala.");
		}
		PreguntaDocente pregunta = preguntaDocenteRepository
				.findById(request.preguntaId())
				.filter(PreguntaDocente::isActiva)
				.orElseThrow(() -> new ResourceNotFoundException(
						"La pregunta no existe."));
		if (!quizItemRepository.existsByQuiz_IdAndPregunta_Id(
				sala.getQuizOrigen().getId(), pregunta.getId())) {
			throw new SolicitudInvalidaException(
					"La pregunta no pertenece al quiz de la sala.");
		}
		if (anulacionPreguntaRepository.existsBySala_IdAndPregunta_Id(
				salaId, pregunta.getId())) {
			throw new SolicitudInvalidaException(
					"La pregunta fue anulada y no recibe respuestas.");
		}
		if (respuestaSalaRepository.existsByParticipacion_IdAndPregunta_Id(
				participacion.getId(), pregunta.getId())) {
			throw new SolicitudInvalidaException(
					"Cada pregunta admite un único envío definitivo.");
		}
		boolean omitida = request.omitida();
		boolean acierto = !omitida && request.acierto();
		RespuestaSala respuesta = new RespuestaSala(participacion, pregunta,
				acierto, omitida);
		respuesta.setTiempoMs(request.tiempoMs());
		return aResponse(respuestaSalaRepository.save(respuesta));
	}

	@Override
	@Transactional(readOnly = true)
	public RespuestaSalaResponse mostrarPorId(Long salaId, Long id) {
		validarIdentificador(id);
		buscarSala(salaId);
		RespuestaSala respuesta = respuestaSalaRepository.findById(id)
				.filter(r -> salaId.equals(
						r.getParticipacion().getSala().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La respuesta no existe."));
		return aResponse(respuesta);
	}

	@Override
	@Transactional(readOnly = true)
	public RespuestaSalaResponse mostrarPorParticipacionYPregunta(
			Long salaId, Long participacionId, Long preguntaId) {
		validarIdentificador(participacionId);
		validarIdentificador(preguntaId);
		buscarSala(salaId);
		RespuestaSala respuesta = respuestaSalaRepository
				.findByParticipacion_IdAndPregunta_Id(participacionId, preguntaId)
				.filter(r -> salaId.equals(
						r.getParticipacion().getSala().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La respuesta no existe."));
		return aResponse(respuesta);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RespuestaSalaResponse> listarPorParticipacion(
			Long salaId, Long participacionId) {
		validarIdentificador(participacionId);
		buscarSala(salaId);
		participacionSalaRepository.findById(participacionId)
				.filter(p -> salaId.equals(p.getSala().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La participación no existe."));
		return respuestaSalaRepository
				.findAllByParticipacion_Id(participacionId).stream()
				.map(this::aResponse)
				.toList();
	}

	private Sala buscarSala(Long salaId) {
		validarIdentificador(salaId);
		return salaRepository.findById(salaId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"La sala no existe."));
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private RespuestaSalaResponse aResponse(RespuestaSala respuesta) {
		return new RespuestaSalaResponse(
				respuesta.getId(),
				respuesta.getParticipacion().getId(),
				respuesta.getPregunta().getId(),
				respuesta.isAcierto(),
				respuesta.isOmitida(),
				respuesta.getTiempoMs());
	}
}
