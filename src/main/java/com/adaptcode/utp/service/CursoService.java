package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.response.CursoResponseDTO;

public interface CursoService {

	List<CursoResponseDTO> obtenerCursosActivos();
}
