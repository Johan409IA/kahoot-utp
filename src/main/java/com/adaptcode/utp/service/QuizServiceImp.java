package com.adaptcode.utp.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.QuizItemRequest;
import com.adaptcode.utp.dto.request.QuizRequest;
import com.adaptcode.utp.dto.response.QuizItemResponse;
import com.adaptcode.utp.dto.response.QuizResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.AlternativaDocente;
import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.Quiz;
import com.adaptcode.utp.model.QuizItem;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.AlternativaDocenteRepository;
import com.adaptcode.utp.repository.PreguntaDocenteRepository;
import com.adaptcode.utp.repository.QuizRepository;
import com.adaptcode.utp.repository.RetroalimentacionDocenteRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizServiceImp implements QuizService {

	private final QuizRepository quizRepository;
	private final UsuarioRepository usuarioRepository;
	private final PreguntaDocenteRepository preguntaDocenteRepository;
	private final AlternativaDocenteRepository alternativaDocenteRepository;
	private final RetroalimentacionDocenteRepository retroalimentacionDocenteRepository;

	@Override
	@Transactional
	public QuizResponse crearQuiz(Long docenteId, QuizRequest request) {
		Usuario docente = validarDocente(docenteId);
		Quiz quiz = new Quiz(docente, request.titulo());
		agregarItems(quiz, request.items());
		return aResponse(quizRepository.save(quiz));
	}

	@Override
	@Transactional(readOnly = true)
	public QuizResponse mostrarPorId(Long docenteId, Long id) {
		validarIdentificador(id);
		return aResponse(buscarPropio(docenteId, id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<QuizResponse> listarPorDocente(Long docenteId, EstadoQuiz estado) {
		validarDocente(docenteId);
		List<Quiz> quizzes = estado == null
				? quizRepository.findAllByDocente_IdOrderByIdDesc(docenteId)
				: quizRepository.findAllByDocente_IdAndEstadoOrderByIdDesc(
						docenteId, estado);
		return quizzes.stream()
				.map(this::aResponse)
				.toList();
	}

	@Override
	@Transactional
	public QuizResponse actualizarQuiz(Long docenteId, Long id, QuizRequest request) {
		Quiz quiz = buscarPropio(docenteId, id);
		if (quiz.getEstado() != EstadoQuiz.BORRADOR) {
			throw new SolicitudInvalidaException(
					"Solo un quiz en Borrador se puede modificar.");
		}
		quiz.setTitulo(request.titulo());
		quiz.getItems().clear();
		agregarItems(quiz, request.items());
		return aResponse(quizRepository.save(quiz));
	}

	@Override
	@Transactional
	public void eliminarQuiz(Long docenteId, Long id) {
		Quiz quiz = buscarPropio(docenteId, id);
		if (quiz.getEstado() != EstadoQuiz.BORRADOR) {
			throw new SolicitudInvalidaException(
					"Solo un quiz en Borrador se puede eliminar.");
		}
		quizRepository.delete(quiz);
	}

	@Override
	@Transactional
	public QuizResponse marcarQuizListo(Long docenteId, Long id) {
		Quiz quiz = buscarPropio(docenteId, id);
		if (quiz.getEstado() != EstadoQuiz.BORRADOR) {
			throw new SolicitudInvalidaException("El quiz ya está Listo para usar.");
		}
		if (quiz.getItems().isEmpty()) {
			throw new SolicitudInvalidaException(
					"El quiz necesita al menos una pregunta para quedar Listo.");
		}
		boolean todasActivas = quiz.getItems().stream()
				.allMatch(item -> item.getPregunta().isActiva()
						&& docenteId.equals(item.getPregunta().getDocente().getId()));
		if (!todasActivas) {
			throw new SolicitudInvalidaException(
					"Todas las preguntas deben pertenecer al docente y estar activas.");
		}
		for (QuizItem item : quiz.getItems()) {
			validarFeedbackCompleto(item.getPregunta().getId());
		}
		quiz.setEstado(EstadoQuiz.LISTO);
		return aResponse(quizRepository.save(quiz));
	}

	private void validarFeedbackCompleto(Long preguntaId) {
		List<AlternativaDocente> alternativas = alternativaDocenteRepository
				.findAllByPregunta_IdOrderByOrdenVisualAsc(preguntaId);
		if (alternativas.isEmpty()) {
			throw new SolicitudInvalidaException(
					"La pregunta " + preguntaId + " no tiene alternativas.");
		}
		if (alternativas.stream().noneMatch(AlternativaDocente::isCorrecta)) {
			throw new SolicitudInvalidaException(
					"La pregunta " + preguntaId + " debe tener una alternativa correcta.");
		}
		boolean incompleta = alternativas.stream()
				.filter(a -> !a.isCorrecta())
				.anyMatch(a -> !retroalimentacionDocenteRepository.existsByAlternativa_Id(
						a.getId()));
		if (incompleta) {
			throw new SolicitudInvalidaException(
					"La pregunta " + preguntaId
							+ " no tiene retroalimentación completa (Q19).");
		}
	}

	private void agregarItems(Quiz quiz, List<QuizItemRequest> items) {		if (items == null) {
			return;
		}
		Set<Long> preguntasVistas = new HashSet<>();
		Set<Integer> ordenesVistos = new HashSet<>();
		for (QuizItemRequest item : items) {
			if (!preguntasVistas.add(item.preguntaId())) {
				throw new SolicitudInvalidaException(
						"El quiz no puede repetir la misma pregunta.");
			}
			if (!ordenesVistos.add(item.orden())) {
				throw new SolicitudInvalidaException("El orden no se puede repetir.");
			}
			PreguntaDocente pregunta = preguntaDocenteRepository.findById(item.preguntaId())
					.filter(p -> p.isActiva()
							&& quiz.getDocente().getId().equals(p.getDocente().getId()))
					.orElseThrow(() -> new ResourceNotFoundException(
							"La pregunta " + item.preguntaId() + " no existe."));
			quiz.agregarItem(new QuizItem(quiz, pregunta, item.orden()));
		}
	}

	private Quiz buscarPropio(Long docenteId, Long id) {
		validarDocente(docenteId);
		return quizRepository.buscarConItemsPorIdYDocente(id, docenteId)
				.orElseThrow(() -> new ResourceNotFoundException("El quiz no existe."));
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

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private QuizResponse aResponse(Quiz quiz) {
		List<QuizItemResponse> items = quiz.getItems().stream()
				.map(item -> new QuizItemResponse(
						item.getId(),
						item.getPregunta().getId(),
						item.getPregunta().getEnunciado(),
						item.getOrden()))
				.toList();
		return new QuizResponse(
				quiz.getId(), quiz.getDocente().getId(),
				quiz.getTitulo(), quiz.getEstado(), items);
	}
}
