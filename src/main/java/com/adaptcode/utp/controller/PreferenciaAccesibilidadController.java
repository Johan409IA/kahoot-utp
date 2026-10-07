package com.adaptcode.utp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.PreferenciaAccesibilidadRequest;
import com.adaptcode.utp.dto.response.PreferenciaAccesibilidadResponse;
import com.adaptcode.utp.service.PreferenciaAccesibilidadService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/preferencias")
@Validated
@RequiredArgsConstructor
public class PreferenciaAccesibilidadController {

	private final PreferenciaAccesibilidadService preferenciaAccesibilidadService;

	@GetMapping
	public ResponseEntity<PreferenciaAccesibilidadResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId) {
		return ResponseEntity.ok(
				preferenciaAccesibilidadService.mostrarPorUsuario(usuarioId));
	}

	@PutMapping
	public ResponseEntity<PreferenciaAccesibilidadResponse> guardar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId,
			@Valid @RequestBody PreferenciaAccesibilidadRequest request) {
		return ResponseEntity.ok(preferenciaAccesibilidadService.guardarPreferencias(
				usuarioId, request));
	}
}
