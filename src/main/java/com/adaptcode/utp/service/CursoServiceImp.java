package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.CursoResponseDTO;
import com.adaptcode.utp.repository.CursoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CursoServiceImp implements CursoService {

	private final CursoRepository cursoRepository;

	@Override
	@Transactional(readOnly = true)
	public List<CursoResponseDTO> obtenerCursosActivos() {
		return cursoRepository.findAllByActivoTrueOrderByNombreAsc().stream()
				.map(curso -> new CursoResponseDTO(curso.getId(), curso.getNombre()))
				.toList();
	}
}
