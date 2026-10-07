package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.response.TemaResponseDTO;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
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
			throw new ResourceNotFoundException("El curso no existe.");
		}

		return temaRepository.findAllByCurso_IdAndActivoTrueOrderByNombreAsc(cursoId).stream()
				.map(tema -> new TemaResponseDTO(tema.getId(), tema.getNombre()))
				.toList();
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}
}
