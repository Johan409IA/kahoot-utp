package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.response.InsigniaResponse;
import com.adaptcode.utp.service.InsigniaObtenidaService;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/insignias")
@Validated
@RequiredArgsConstructor
public class InsigniaObtenidaController {

	private final InsigniaObtenidaService insigniaObtenidaService;

	@GetMapping
	public ResponseEntity<List<InsigniaResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId) {
		return ResponseEntity.ok(
				insigniaObtenidaService.listarPorUsuario(usuarioId));
	}
}
