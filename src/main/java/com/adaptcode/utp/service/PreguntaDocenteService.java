package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.PreguntaDocenteRequest;
import com.adaptcode.utp.dto.response.PreguntaDocenteResponse;
import com.adaptcode.utp.model.OrigenPregunta;

public interface PreguntaDocenteService {

	PreguntaDocenteResponse crearPreguntaPropia(Long docenteId, PreguntaDocenteRequest request);

	PreguntaDocenteResponse clonarPreguntaInstitucional(Long docenteId, Long microRetoId);

	PreguntaDocenteResponse mostrarPorId(Long docenteId, Long id);

	List<PreguntaDocenteResponse> listarPorDocente(Long docenteId, OrigenPregunta origen);

	PreguntaDocenteResponse actualizarPregunta(Long docenteId, Long id, PreguntaDocenteRequest request);

	void eliminarPregunta(Long docenteId, Long id);
}
