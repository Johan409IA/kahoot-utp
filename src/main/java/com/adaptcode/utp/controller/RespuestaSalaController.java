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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.RespuestaSalaRequest;
import com.adaptcode.utp.dto.response.RespuestaSalaResponse;
import com.adaptcode.utp.service.RespuestaSalaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salas/{salaId}/respuestas")
@Validated
@RequiredArgsConstructor
public class RespuestaSalaController {

	private final RespuestaSalaService respuestaSalaService;

	@PostMapping
	public ResponseEntity<RespuestaSalaResponse> registrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Valid @RequestBody RespuestaSalaRequest request) {
		RespuestaSalaResponse response = respuestaSalaService.registrar(
				salaId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<RespuestaSalaResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Positive(message = "El identificador debe ser positivo.") @RequestParam Long participacionId) {
		return ResponseEntity.ok(respuestaSalaService.listarPorParticipacion(
				salaId, participacionId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RespuestaSalaResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(
				respuestaSalaService.mostrarPorId(salaId, id));
	}

	@GetMapping("/por-pregunta")
	public ResponseEntity<RespuestaSalaResponse> mostrarPorPregunta(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Positive(message = "El identificador debe ser positivo.") @RequestParam Long participacionId,
			@Positive(message = "El identificador debe ser positivo.") @RequestParam Long preguntaId) {
		return ResponseEntity.ok(respuestaSalaService.mostrarPorParticipacionYPregunta(
				salaId, participacionId, preguntaId));
	}
}
