package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.AnulacionPreguntaRequest;
import com.adaptcode.utp.dto.response.AnulacionPreguntaResponse;
import com.adaptcode.utp.service.AnulacionPreguntaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/docentes/{docenteId}/salas/{salaId}/anulaciones")
@Validated
@RequiredArgsConstructor
public class AnulacionPreguntaController {

	private final AnulacionPreguntaService anulacionPreguntaService;

	@PostMapping
	public ResponseEntity<AnulacionPreguntaResponse> anular(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Valid @RequestBody AnulacionPreguntaRequest request) {
		AnulacionPreguntaResponse response = anulacionPreguntaService.anular(
				docenteId, salaId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<AnulacionPreguntaResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long docenteId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId) {
		return ResponseEntity.ok(
				anulacionPreguntaService.listarPorSala(docenteId, salaId));
	}
}
