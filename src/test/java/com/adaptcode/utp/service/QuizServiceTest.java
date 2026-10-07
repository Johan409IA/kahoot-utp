package com.adaptcode.utp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.adaptcode.utp.dto.request.QuizItemRequest;
import com.adaptcode.utp.dto.request.QuizRequest;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.AlternativaDocente;
import com.adaptcode.utp.model.Curso;
import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.OrigenPregunta;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.Quiz;
import com.adaptcode.utp.model.QuizItem;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.model.TipoMicroReto;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.AlternativaDocenteRepository;
import com.adaptcode.utp.repository.PreguntaDocenteRepository;
import com.adaptcode.utp.repository.QuizRepository;
import com.adaptcode.utp.repository.RetroalimentacionDocenteRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

class QuizServiceTest {

	private QuizRepository quizRepository;
	private UsuarioRepository usuarioRepository;
	private PreguntaDocenteRepository preguntaDocenteRepository;
	private AlternativaDocenteRepository alternativaDocenteRepository;
	private RetroalimentacionDocenteRepository retroalimentacionDocenteRepository;
	private QuizServiceImp quizService;

	@BeforeEach
	void configurar() {
		quizRepository = mock(QuizRepository.class);
		usuarioRepository = mock(UsuarioRepository.class);
		preguntaDocenteRepository = mock(PreguntaDocenteRepository.class);
		alternativaDocenteRepository = mock(AlternativaDocenteRepository.class);
		retroalimentacionDocenteRepository = mock(
				RetroalimentacionDocenteRepository.class);
		quizService = new QuizServiceImp(quizRepository, usuarioRepository,
				preguntaDocenteRepository, alternativaDocenteRepository,
				retroalimentacionDocenteRepository);
	}

	@Test
	void crearQuizConItems_yMarcarListoConFeedbackCompleto() {
		Usuario docente = docenteMock(7L);
		PreguntaDocente pregunta = preguntaConId(docente, 50L);
		when(usuarioRepository.findById(7L)).thenReturn(Optional.of(docente));
		when(preguntaDocenteRepository.findById(50L))
				.thenReturn(Optional.of(pregunta));
		AtomicReference<Quiz> guardado = new AtomicReference<>();
		when(quizRepository.save(any())).thenAnswer(inv -> {
			Quiz q = inv.getArgument(0);
			if (q.getId() == null) {
				q.setId(100L);
			}
			guardado.set(q);
			return q;
		});
		when(quizRepository.buscarConItemsPorIdYDocente(100L, 7L))
				.thenAnswer(inv -> Optional.ofNullable(guardado.get()));
		AlternativaDocente incorrecta = new AlternativaDocente(pregunta, "Falso",
				null, null, 1, false);
		ReflectionTestUtils.setField(incorrecta, "id", 200L);
		AlternativaDocente correcta = new AlternativaDocente(pregunta, "Verdadero",
				null, null, 2, true);
		ReflectionTestUtils.setField(correcta, "id", 201L);
		when(alternativaDocenteRepository.findAllByPregunta_IdOrderByOrdenVisualAsc(
				50L)).thenReturn(List.of(incorrecta, correcta));
		when(retroalimentacionDocenteRepository.existsByAlternativa_Id(200L))
				.thenReturn(true);

		var creado = quizService.crearQuiz(7L, new QuizRequest("Quiz demo",
				List.of(new QuizItemRequest(50L, 1))));
		var listo = quizService.marcarQuizListo(7L, creado.id());

		assertEquals(EstadoQuiz.BORRADOR, creado.estado());
		assertEquals(EstadoQuiz.LISTO, listo.estado());
	}

	@Test
	void marcarListo_sinFeedbackCompleto_rechazaPorQ19() {
		Usuario docente = docenteMock(7L);
		PreguntaDocente pregunta = preguntaConId(docente, 50L);
		Quiz quiz = new Quiz(docente, "Quiz demo");
		quiz.setId(100L);
		quiz.agregarItem(new QuizItem(quiz, pregunta, 1));
		when(usuarioRepository.findById(7L)).thenReturn(Optional.of(docente));
		when(quizRepository.buscarConItemsPorIdYDocente(100L, 7L))
				.thenReturn(Optional.of(quiz));
		AlternativaDocente incorrecta = new AlternativaDocente(pregunta, "Falso",
				null, null, 1, false);
		ReflectionTestUtils.setField(incorrecta, "id", 200L);
		AlternativaDocente correcta = new AlternativaDocente(pregunta, "Verdadero",
				null, null, 2, true);
		ReflectionTestUtils.setField(correcta, "id", 201L);
		when(alternativaDocenteRepository.findAllByPregunta_IdOrderByOrdenVisualAsc(
				50L)).thenReturn(List.of(incorrecta, correcta));
		when(retroalimentacionDocenteRepository.existsByAlternativa_Id(200L))
				.thenReturn(false);

		assertThrows(SolicitudInvalidaException.class,
				() -> quizService.marcarQuizListo(7L, 100L));
	}

	private Usuario docenteMock(Long id) {
		Usuario docente = mock(Usuario.class);
		when(docente.getId()).thenReturn(id);
		when(docente.getRol()).thenReturn(RolUsuario.DOCENTE_UTP);
		when(docente.isActivo()).thenReturn(true);
		return docente;
	}

	private PreguntaDocente preguntaConId(Usuario docente, Long id) {
		Curso curso = new Curso("Algoritmos");
		ReflectionTestUtils.setField(curso, "id", 1L);
		Tema tema = new Tema(curso, "Condicionales");
		ReflectionTestUtils.setField(tema, "id", 10L);
		PreguntaDocente pregunta = new PreguntaDocente(docente,
				OrigenPregunta.PROPIA, tema, TipoMicroReto.SELECCION_MULTIPLE,
				NivelMicroReto.BASICO, "Evalúa una condición.");
		ReflectionTestUtils.setField(pregunta, "id", id);
		return pregunta;
	}
}
