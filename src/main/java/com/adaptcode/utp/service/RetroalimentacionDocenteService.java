package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.RetroalimentacionDocenteRequest;
import com.adaptcode.utp.dto.response.RetroalimentacionDocenteResponse;

public interface RetroalimentacionDocenteService {

	RetroalimentacionDocenteResponse crearRetroalimentacion(
			Long docenteId, Long preguntaId, RetroalimentacionDocenteRequest request);

	RetroalimentacionDocenteResponse mostrarPorId(
			Long docenteId, Long preguntaId, Long id);

	List<RetroalimentacionDocenteResponse> listarPorPregunta(
			Long docenteId, Long preguntaId);

	RetroalimentacionDocenteResponse actualizarRetroalimentacion(
			Long docenteId, Long preguntaId, Long id,
			RetroalimentacionDocenteRequest request);

	void eliminarRetroalimentacion(Long docenteId, Long preguntaId, Long id);
}
