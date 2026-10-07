package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.IntentoRequest;
import com.adaptcode.utp.dto.response.IntentoResponse;

public interface IntentoService {

	IntentoResponse registrarIntento(Long usuarioId, IntentoRequest request);

	IntentoResponse mostrarPorId(Long usuarioId, Long id);

	List<IntentoResponse> listarPorUsuarioYReto(Long usuarioId, Long microRetoId);
}
