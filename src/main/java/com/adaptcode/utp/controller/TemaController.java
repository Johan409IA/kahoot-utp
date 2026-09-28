package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.TemaResponseDTO;
import com.adaptcode.utp.service.TemaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cursos/{cursoId}/temas")
@RequiredArgsConstructor
public class TemaController {

	private final TemaService temaService;

	@GetMapping
	public ResponseEntity<List<TemaResponseDTO>> obtenerTemas(@PathVariable Long cursoId) {
		return ResponseEntity.ok(temaService.obtenerTemasActivosPorCurso(cursoId));
	}
}
