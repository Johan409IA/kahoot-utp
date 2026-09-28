package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.adaptcode.utp.dto.TemaResponseDTO;
import com.adaptcode.utp.repository.CursoRepository;
import com.adaptcode.utp.repository.TemaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemaServiceImp implements TemaService {

	private final CursoRepository cursoRepository;
	private final TemaRepository temaRepository;

	@Override
	@Transactional(readOnly = true)
	public List<TemaResponseDTO> obtenerTemasActivosPorCurso(Long cursoId) {
		validarIdentificador(cursoId);
		if (!cursoRepository.existsByIdAndActivoTrue(cursoId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El curso no existe.");
		}

		return temaRepository.findAllByCurso_IdAndActivoTrueOrderByNombreAsc(cursoId).stream()
				.map(tema -> new TemaResponseDTO(tema.getId(), tema.getNombre()))
				.toList();
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador debe ser positivo.");
		}
	}
}
