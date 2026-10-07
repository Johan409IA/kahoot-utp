package com.adaptcode.utp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptcode.utp.dto.request.MicroRetoFiltroRequest;
import com.adaptcode.utp.dto.response.MicroRetoDetalleResponseDTO;
import com.adaptcode.utp.dto.response.MicroRetoResumenResponseDTO;
import com.adaptcode.utp.service.MicroRetoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/micro-retos")
@Validated
@RequiredArgsConstructor
public class MicroRetoController {

	private final MicroRetoService microRetoService;

	@GetMapping
	public ResponseEntity<List<MicroRetoResumenResponseDTO>> listar(
			@Valid @ModelAttribute MicroRetoFiltroRequest filtro) {
		return ResponseEntity.ok(microRetoService.obtenerResumenes(
				filtro.cursoId(), filtro.temaId(), filtro.nivel()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MicroRetoDetalleResponseDTO> obtenerDetalle(
			@Positive(message = "El identificador debe ser positivo.") @PathVariable Long id) {
		return ResponseEntity.ok(microRetoService.obtenerDetalle(id));
	}
}
