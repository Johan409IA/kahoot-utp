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

import com.adaptcode.utp.dto.request.AlternativaDocenteRequest;
import com.adaptcode.utp.dto.response.AlternativaDocenteResponse;
import com.adaptcode.utp.service.AlternativaDocenteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/docentes/{docenteId}/preguntas/{preguntaId}/alternativas")
@Validated
@RequiredArgsConstructor
public class AlternativaDocenteController {

	private final AlternativaDocenteService alternativaDocenteService;

	@PostMapping
	public ResponseEntity<AlternativaDocenteResponse> crear(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Valid @RequestBody AlternativaDocenteRequest request) {
		AlternativaDocenteResponse response = alternativaDocenteService
				.crearAlternativa(docenteId, preguntaId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<AlternativaDocenteResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId) {
		return ResponseEntity.ok(
				alternativaDocenteService.listarPorPregunta(docenteId, preguntaId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AlternativaDocenteResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(
				alternativaDocenteService.mostrarPorId(docenteId, preguntaId, id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AlternativaDocenteResponse> actualizar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id,
			@Valid @RequestBody AlternativaDocenteRequest request) {
		return ResponseEntity.ok(alternativaDocenteService.actualizarAlternativa(
				docenteId, preguntaId, id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> eliminar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long preguntaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		alternativaDocenteService.eliminarAlternativa(docenteId, preguntaId, id);
		return ResponseEntity.ok("Alternativa eliminada.");
	}
}
