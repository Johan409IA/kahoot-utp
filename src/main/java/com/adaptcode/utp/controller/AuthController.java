package com.adaptcode.utp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.LoginRequest;
import com.adaptcode.utp.dto.request.RecuperarContrasenaRequest;
import com.adaptcode.utp.dto.request.ReenviarCodigoRequest;
import com.adaptcode.utp.dto.request.RegistroRequest;
import com.adaptcode.utp.dto.request.RestablecerContrasenaRequest;
import com.adaptcode.utp.dto.request.VerificarCodigoRequest;
import com.adaptcode.utp.dto.response.AuthResponse;
import com.adaptcode.utp.dto.response.CodigoResponse;
import com.adaptcode.utp.dto.response.MensajeResponse;
import com.adaptcode.utp.dto.response.RegistroResponse;
import com.adaptcode.utp.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/registro")
	public ResponseEntity<RegistroResponse> registrar(
			@Valid @RequestBody RegistroRequest request) {
		return new ResponseEntity<>(authService.registrar(request), HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/verificar-cuenta")
	public ResponseEntity<MensajeResponse> verificarCuenta(
			@Valid @RequestBody VerificarCodigoRequest request) {
		return ResponseEntity.ok(authService.verificarCuenta(request));
	}

	@PostMapping("/reenviar-codigo")
	public ResponseEntity<CodigoResponse> reenviarCodigo(
			@Valid @RequestBody ReenviarCodigoRequest request) {
		return ResponseEntity.ok(authService.reenviarCodigoVerificacion(request));
	}

	@PostMapping("/recuperar-contrasena")
	public ResponseEntity<CodigoResponse> recuperarContrasena(
			@Valid @RequestBody RecuperarContrasenaRequest request) {
		return ResponseEntity.ok(authService.solicitarRecuperacion(request));
	}

	@PostMapping("/restablecer-contrasena")
	public ResponseEntity<MensajeResponse> restablecerContrasena(
			@Valid @RequestBody RestablecerContrasenaRequest request) {
		return ResponseEntity.ok(authService.restablecerContrasena(request));
	}
}
