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
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.RetroalimentacionDocenteRequest;
import com.adaptcode.utp.dto.response.RetroalimentacionDocenteResponse;
import com.adaptcode.utp.service.RetroalimentacionDocenteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/docentes/{docenteId}/preguntas/{preguntaId}/retroalimentaciones")
@Validated
@RequiredArgsConstructor
public class RetroalimentacionDocenteController {

	private final RetroalimentacionDocenteService retroalimentacionDocenteService;

	@PostMapping
	public ResponseEntity<RetroalimentacionDocenteResponse> crear(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Valid @RequestBody RetroalimentacionDocenteRequest request) {
		RetroalimentacionDocenteResponse response = retroalimentacionDocenteService
				.crearRetroalimentacion(docenteId, preguntaId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<RetroalimentacionDocenteResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId) {
		return ResponseEntity.ok(retroalimentacionDocenteService.listarPorPregunta(
				docenteId, preguntaId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RetroalimentacionDocenteResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(retroalimentacionDocenteService.mostrarPorId(
				docenteId, preguntaId, id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<RetroalimentacionDocenteResponse> actualizar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id,
			@Valid @RequestBody RetroalimentacionDocenteRequest request) {
		return ResponseEntity.ok(
				retroalimentacionDocenteService.actualizarRetroalimentacion(
						docenteId, preguntaId, id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> eliminar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		retroalimentacionDocenteService.eliminarRetroalimentacion(
				docenteId, preguntaId, id);
		return ResponseEntity.ok("Retroalimentación eliminada.");
	}
}
