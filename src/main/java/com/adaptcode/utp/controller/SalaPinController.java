package com.adaptcode.utp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.response.SalaResponse;
import com.adaptcode.utp.service.SalaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salas")
@Validated
@RequiredArgsConstructor
public class SalaPinController {

	private final SalaService salaService;

	@GetMapping("/por-pin")
	public ResponseEntity<SalaResponse> mostrarPorPin(
			@RequestParam String pin) {
		return ResponseEntity.ok(salaService.mostrarPorPin(pin));
	}
}
