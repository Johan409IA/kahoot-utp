package com.adaptcode.utp.service;

import com.adaptcode.utp.dto.request.EvaluarIntentoRequest;
import com.adaptcode.utp.dto.response.EvaluarIntentoResponse;

public interface EvaluationService {

	EvaluarIntentoResponse evaluarIntento(Long usuarioId, EvaluarIntentoRequest request);
}
