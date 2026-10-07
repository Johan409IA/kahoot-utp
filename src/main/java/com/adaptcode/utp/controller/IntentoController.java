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

import com.adaptcode.utp.dto.request.IntentoRequest;
import com.adaptcode.utp.dto.response.IntentoResponse;
import com.adaptcode.utp.service.IntentoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/intentos")
@Validated
@RequiredArgsConstructor
public class IntentoController {

	private final IntentoService intentoService;

	@PostMapping
	public ResponseEntity<IntentoResponse> registrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId,
			@Valid @RequestBody IntentoRequest request) {
		IntentoResponse response = intentoService.registrarIntento(usuarioId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<IntentoResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId,
			@Positive(message = "El identificador debe ser positivo.") @RequestParam Long microRetoId) {
		return ResponseEntity.ok(intentoService.listarPorUsuarioYReto(usuarioId, microRetoId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<IntentoResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(intentoService.mostrarPorId(usuarioId, id));
	}
}
