package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.CursoResponseDTO;
import com.adaptcode.utp.service.CursoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {

	private final CursoService cursoService;

	@GetMapping
	public ResponseEntity<List<CursoResponseDTO>> obtenerCursos() {
		return ResponseEntity.ok(cursoService.obtenerCursosActivos());
	}
}
