package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.RespuestaSalaRequest;
import com.adaptcode.utp.dto.response.RespuestaSalaResponse;

public interface RespuestaSalaService {

	RespuestaSalaResponse registrar(Long salaId, RespuestaSalaRequest request);

	RespuestaSalaResponse mostrarPorId(Long salaId, Long id);

	RespuestaSalaResponse mostrarPorParticipacionYPregunta(
			Long salaId, Long participacionId, Long preguntaId);

	List<RespuestaSalaResponse> listarPorParticipacion(Long salaId, Long participacionId);
}
