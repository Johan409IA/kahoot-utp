package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.TemaResponseDTO;

public interface TemaService {

	List<TemaResponseDTO> obtenerTemasActivosPorCurso(Long cursoId);
}
