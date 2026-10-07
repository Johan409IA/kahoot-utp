package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.AlternativaDocenteRequest;
import com.adaptcode.utp.dto.response.AlternativaDocenteResponse;

public interface AlternativaDocenteService {

	AlternativaDocenteResponse crearAlternativa(
			Long docenteId, Long preguntaId, AlternativaDocenteRequest request);

	AlternativaDocenteResponse mostrarPorId(Long docenteId, Long preguntaId, Long id);

	List<AlternativaDocenteResponse> listarPorPregunta(Long docenteId, Long preguntaId);

	AlternativaDocenteResponse actualizarAlternativa(
			Long docenteId, Long preguntaId, Long id, AlternativaDocenteRequest request);

	void eliminarAlternativa(Long docenteId, Long preguntaId, Long id);
}
