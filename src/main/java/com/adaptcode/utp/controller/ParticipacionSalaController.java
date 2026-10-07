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

import com.adaptcode.utp.dto.request.ParticipacionSalaRequest;
import com.adaptcode.utp.dto.response.ParticipacionSalaResponse;
import com.adaptcode.utp.service.ParticipacionSalaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/salas/{salaId}/participaciones")
@Validated
@RequiredArgsConstructor
public class ParticipacionSalaController {

	private final ParticipacionSalaService participacionSalaService;

	@PostMapping
	public ResponseEntity<ParticipacionSalaResponse> unirse(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Valid @RequestBody ParticipacionSalaRequest request) {
		ParticipacionSalaResponse response = participacionSalaService.unirse(
				salaId, request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<ParticipacionSalaResponse>> listar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId) {
		return ResponseEntity.ok(
				participacionSalaService.listarPorSala(salaId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ParticipacionSalaResponse> mostrar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(
				participacionSalaService.mostrarPorId(salaId, id));
	}

	@PostMapping("/{id}/expulsar")
	public ResponseEntity<ParticipacionSalaResponse> expulsar(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id,
			@Positive(message = "El identificador debe ser positivo.") @RequestParam Long docenteId) {
		return ResponseEntity.ok(
				participacionSalaService.expulsar(docenteId, salaId, id));
	}

	@PostMapping("/{id}/readmitir")
	public ResponseEntity<ParticipacionSalaResponse> readmitir(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long salaId,
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id,
			@Positive(message = "El identificador debe ser positivo.") @RequestParam Long docenteId) {
		return ResponseEntity.ok(
				participacionSalaService.readmitir(docenteId, salaId, id));
	}
}
