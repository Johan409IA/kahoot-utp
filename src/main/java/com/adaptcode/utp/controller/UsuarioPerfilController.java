package com.adaptcode.utp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.UsuarioPerfilRequest;
import com.adaptcode.utp.dto.response.UsuarioPerfilResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.security.UsuarioPrincipal;
import com.adaptcode.utp.service.UsuarioPerfilService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/perfil")
@Validated
@RequiredArgsConstructor
public class UsuarioPerfilController {

	private final UsuarioPerfilService usuarioPerfilService;

	@PutMapping
	public ResponseEntity<UsuarioPerfilResponse> completar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long usuarioId,
			@AuthenticationPrincipal UsuarioPrincipal principal,
			@Valid @RequestBody UsuarioPerfilRequest request) {
		if (principal == null || !usuarioId.equals(principal.getId())) {
			throw new ResourceNotFoundException("El usuario no existe.");
		}
		return ResponseEntity.ok(usuarioPerfilService.completar(usuarioId, request));
	}
}
