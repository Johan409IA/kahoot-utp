package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.RetroalimentacionDocenteRequest;
import com.adaptcode.utp.dto.response.RetroalimentacionDocenteResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.AlternativaDocente;
import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.OrigenPregunta;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.RetroalimentacionDocente;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.AlternativaDocenteRepository;
import com.adaptcode.utp.repository.PreguntaDocenteRepository;
import com.adaptcode.utp.repository.QuizItemRepository;
import com.adaptcode.utp.repository.RetroalimentacionDocenteRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetroalimentacionDocenteServiceImp
		implements RetroalimentacionDocenteService {

	private final RetroalimentacionDocenteRepository retroalimentacionDocenteRepository;
	private final AlternativaDocenteRepository alternativaDocenteRepository;
	private final PreguntaDocenteRepository preguntaDocenteRepository;
	private final UsuarioRepository usuarioRepository;
	private final QuizItemRepository quizItemRepository;

	@Override
	@Transactional
	public RetroalimentacionDocenteResponse crearRetroalimentacion(
			Long docenteId, Long preguntaId, RetroalimentacionDocenteRequest request) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		validarEditable(pregunta);
		AlternativaDocente alternativa = buscarAlternativaDePregunta(
				pregunta, request.alternativaId());
		if (alternativa.isCorrecta()) {
			throw new SolicitudInvalidaException(
					"Solo las alternativas incorrectas llevan retroalimentación.");
		}
		if (retroalimentacionDocenteRepository.existsByAlternativa_Id(
				alternativa.getId())) {
			throw new SolicitudInvalidaException(
					"La alternativa ya tiene retroalimentación; use actualizar.");
		}
		RetroalimentacionDocente retro = new RetroalimentacionDocente(pregunta,
				alternativa, request.causa(), request.explicacion(),
				request.secuenciaTextual(), request.sugerencia());
		return aResponse(retroalimentacionDocenteRepository.save(retro));
	}

	@Override
	@Transactional(readOnly = true)
	public RetroalimentacionDocenteResponse mostrarPorId(
			Long docenteId, Long preguntaId, Long id) {
		validarIdentificador(id);
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		RetroalimentacionDocente retro = retroalimentacionDocenteRepository
				.findById(id)
				.filter(r -> pregunta.getId().equals(r.getPregunta().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La retroalimentación no existe."));
		return aResponse(retro);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RetroalimentacionDocenteResponse> listarPorPregunta(
			Long docenteId, Long preguntaId) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		return retroalimentacionDocenteRepository
				.findAllByPregunta_Id(pregunta.getId()).stream()
				.map(this::aResponse)
				.toList();
	}

	@Override
	@Transactional
	public RetroalimentacionDocenteResponse actualizarRetroalimentacion(
			Long docenteId, Long preguntaId, Long id,
			RetroalimentacionDocenteRequest request) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		validarEditable(pregunta);
		RetroalimentacionDocente retro = retroalimentacionDocenteRepository
				.findById(id)
				.filter(r -> pregunta.getId().equals(r.getPregunta().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La retroalimentación no existe."));
		AlternativaDocente alternativa = buscarAlternativaDePregunta(
				pregunta, request.alternativaId());
		if (alternativa.isCorrecta()) {
			throw new SolicitudInvalidaException(
					"Solo las alternativas incorrectas llevan retroalimentación.");
		}
		if (!alternativa.getId().equals(retro.getAlternativa().getId())
				&& retroalimentacionDocenteRepository.existsByAlternativa_Id(
						alternativa.getId())) {
			throw new SolicitudInvalidaException(
					"La alternativa ya tiene retroalimentación.");
		}
		retro.setAlternativa(alternativa);
		retro.setCausa(request.causa());
		retro.setExplicacion(request.explicacion());
		retro.setSecuenciaTextual(request.secuenciaTextual());
		retro.setSugerencia(request.sugerencia());
		return aResponse(retroalimentacionDocenteRepository.save(retro));
	}

	@Override
	@Transactional
	public void eliminarRetroalimentacion(
			Long docenteId, Long preguntaId, Long id) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		validarEditable(pregunta);
		RetroalimentacionDocente retro = retroalimentacionDocenteRepository
				.findById(id)
				.filter(r -> pregunta.getId().equals(r.getPregunta().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La retroalimentación no existe."));
		retroalimentacionDocenteRepository.delete(retro);
	}

	private AlternativaDocente buscarAlternativaDePregunta(
			PreguntaDocente pregunta, Long alternativaId) {
		validarIdentificador(alternativaId);
		return alternativaDocenteRepository.findById(alternativaId)
				.filter(a -> pregunta.getId().equals(a.getPregunta().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La alternativa no existe."));
	}

	private PreguntaDocente buscarPreguntaPropia(Long docenteId, Long preguntaId) {
		validarDocente(docenteId);
		validarIdentificador(preguntaId);
		return preguntaDocenteRepository.findById(preguntaId)
				.filter(p -> p.isActiva()
						&& docenteId.equals(p.getDocente().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La pregunta no existe."));
	}

	private void validarEditable(PreguntaDocente pregunta) {
		if (pregunta.getOrigen() == OrigenPregunta.INSTITUCIONAL) {
			throw new SolicitudInvalidaException(
					"La pregunta institucional no se puede modificar.");
		}
		if (quizItemRepository.existsByPregunta_IdAndQuiz_Estado(
				pregunta.getId(), EstadoQuiz.LISTO)) {
			throw new SolicitudInvalidaException(
					"La pregunta está usada en un quiz Listo y ya no se puede modificar.");
		}
	}

	private void validarDocente(Long docenteId) {
		validarIdentificador(docenteId);
		Usuario docente = usuarioRepository.findById(docenteId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El docente no existe."));
		if (docente.getRol() != RolUsuario.DOCENTE_UTP || !docente.isActivo()) {
			throw new SolicitudInvalidaException("El usuario no es un docente activo.");
		}
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private RetroalimentacionDocenteResponse aResponse(
			RetroalimentacionDocente retro) {
		return new RetroalimentacionDocenteResponse(
				retro.getId(),
				retro.getPregunta().getId(),
				retro.getAlternativa().getId(),
				retro.getCausa(),
				retro.getExplicacion(),
				retro.getSecuenciaTextual(),
				retro.getSugerencia());
	}
}
