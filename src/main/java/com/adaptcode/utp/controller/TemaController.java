package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.response.TemaResponseDTO;
import com.adaptcode.utp.service.TemaService;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cursos/{cursoId}/temas")
@Validated
@RequiredArgsConstructor
public class TemaController {

	private final TemaService temaService;

	@GetMapping
	public ResponseEntity<List<TemaResponseDTO>> obtenerTemas(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long cursoId) {
		return ResponseEntity.ok(temaService.obtenerTemasActivosPorCurso(cursoId));
	}
}
