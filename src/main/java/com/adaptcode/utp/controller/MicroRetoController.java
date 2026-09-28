package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.MicroRetoDetalleResponseDTO;
import com.adaptcode.utp.dto.MicroRetoResumenResponseDTO;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.service.MicroRetoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/micro-retos")
@RequiredArgsConstructor
public class MicroRetoController {

	private final MicroRetoService microRetoService;

	@GetMapping
	public ResponseEntity<List<MicroRetoResumenResponseDTO>> listar(
			@RequestParam Long cursoId,
			@RequestParam(required = false) Long temaId,
			@RequestParam(required = false) NivelMicroReto nivel) {
		return ResponseEntity.ok(microRetoService.obtenerResumenes(cursoId, temaId, nivel));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MicroRetoDetalleResponseDTO> obtenerDetalle(@PathVariable Long id) {
		return ResponseEntity.ok(microRetoService.obtenerDetalle(id));
	}
}
