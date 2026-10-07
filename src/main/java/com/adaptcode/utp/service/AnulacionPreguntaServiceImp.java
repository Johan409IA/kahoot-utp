package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.AnulacionPreguntaRequest;
import com.adaptcode.utp.dto.response.AnulacionPreguntaResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.AnulacionPregunta;
import com.adaptcode.utp.model.EstadoSala;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.Sala;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.AnulacionPreguntaRepository;
import com.adaptcode.utp.repository.PreguntaDocenteRepository;
import com.adaptcode.utp.repository.QuizItemRepository;
import com.adaptcode.utp.repository.SalaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnulacionPreguntaServiceImp implements AnulacionPreguntaService {

	private final AnulacionPreguntaRepository anulacionPreguntaRepository;
	private final SalaRepository salaRepository;
	private final PreguntaDocenteRepository preguntaDocenteRepository;
	private final QuizItemRepository quizItemRepository;

	@Override
	@Transactional
	public AnulacionPreguntaResponse anular(
			Long docenteId, Long salaId, AnulacionPreguntaRequest request) {
		Sala sala = buscarSalaDeDocente(docenteId, salaId);
		if (sala.getEstado() == EstadoSala.FINALIZADA) {
			throw new SolicitudInvalidaException(
					"No se puede anular con la sala finalizada.");
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
			throw new SolicitudInvalidaException("La pregunta ya fue anulada.");
		}
		Usuario docente = sala.getDocente();
		AnulacionPregunta anulacion = new AnulacionPregunta(sala, pregunta,
				docente, request.motivo());
		return aResponse(anulacionPreguntaRepository.save(anulacion));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AnulacionPreguntaResponse> listarPorSala(Long docenteId, Long salaId) {
		buscarSalaDeDocente(docenteId, salaId);
		return anulacionPreguntaRepository.findAllBySala_Id(salaId).stream()
				.map(this::aResponse)
				.toList();
	}

	private Sala buscarSalaDeDocente(Long docenteId, Long salaId) {
		validarIdentificador(docenteId);
		validarIdentificador(salaId);
		Sala sala = salaRepository.findById(salaId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"La sala no existe."));
		if (!docenteId.equals(sala.getDocente().getId())) {
			throw new ResourceNotFoundException("La sala no existe.");
		}
		return sala;
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private AnulacionPreguntaResponse aResponse(AnulacionPregunta anulacion) {
		return new AnulacionPreguntaResponse(
				anulacion.getId(),
				anulacion.getSala().getId(),
				anulacion.getPregunta().getId(),
				anulacion.getDocente().getId(),
				anulacion.getMotivo());
	}
}
