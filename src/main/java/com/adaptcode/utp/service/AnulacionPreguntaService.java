package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.AnulacionPreguntaRequest;
import com.adaptcode.utp.dto.response.AnulacionPreguntaResponse;

public interface AnulacionPreguntaService {

	AnulacionPreguntaResponse anular(
			Long docenteId, Long salaId, AnulacionPreguntaRequest request);

	List<AnulacionPreguntaResponse> listarPorSala(Long docenteId, Long salaId);
}
