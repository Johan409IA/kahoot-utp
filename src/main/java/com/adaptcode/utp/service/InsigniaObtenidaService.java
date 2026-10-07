package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.response.InsigniaResponse;

public interface InsigniaObtenidaService {

	List<InsigniaResponse> listarPorUsuario(Long usuarioId);
}
