package com.adaptcode.utp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.EvaluarIntentoRequest;
import com.adaptcode.utp.dto.response.EvaluarIntentoResponse;
import com.adaptcode.utp.service.EvaluationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/intentos/evaluar")
@Validated
@RequiredArgsConstructor
public class EvaluacionController {

	private final EvaluationService evaluationService;

	@PostMapping
	public ResponseEntity<EvaluarIntentoResponse> evaluar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId,
			@Valid @RequestBody EvaluarIntentoRequest request) {
		EvaluarIntentoResponse response = evaluationService.evaluarIntento(
				usuarioId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
}
