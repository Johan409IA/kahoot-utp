package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.QuizRequest;
import com.adaptcode.utp.dto.response.QuizResponse;
import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.service.QuizService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/docentes/{docenteId}/quizzes")
@Validated
@RequiredArgsConstructor
public class QuizController {

	private final QuizService quizService;

	@PostMapping
	public ResponseEntity<QuizResponse> crear(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Valid @RequestBody QuizRequest request) {
		QuizResponse response = quizService.crearQuiz(docenteId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<QuizResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@RequestParam(required = false) EstadoQuiz estado) {
		return ResponseEntity.ok(quizService.listarPorDocente(docenteId, estado));
	}

	@GetMapping("/{id}")
	public ResponseEntity<QuizResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(quizService.mostrarPorId(docenteId, id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<QuizResponse> actualizar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id,
			@Valid @RequestBody QuizRequest request) {
		return ResponseEntity.ok(quizService.actualizarQuiz(docenteId, id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> eliminar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		quizService.eliminarQuiz(docenteId, id);
		return ResponseEntity.ok("Quiz eliminado.");
	}

	@PostMapping("/{id}/listo")
	public ResponseEntity<QuizResponse> marcarListo(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(quizService.marcarQuizListo(docenteId, id));
	}
}
