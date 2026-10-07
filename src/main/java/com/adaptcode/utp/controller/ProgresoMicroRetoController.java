package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.response.ProgresoMicroRetoResponse;
import com.adaptcode.utp.service.ProgresoMicroRetoService;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/progreso")
@Validated
@RequiredArgsConstructor
public class ProgresoMicroRetoController {

	private final ProgresoMicroRetoService progresoMicroRetoService;

	@GetMapping
	public ResponseEntity<List<ProgresoMicroRetoResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId) {
		return ResponseEntity.ok(progresoMicroRetoService.listarPorUsuario(usuarioId));
	}

	@GetMapping("/{microRetoId}")
	public ResponseEntity<ProgresoMicroRetoResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long microRetoId) {
		return ResponseEntity.ok(
				progresoMicroRetoService.mostrarPorUsuarioYReto(usuarioId, microRetoId));
	}
}
