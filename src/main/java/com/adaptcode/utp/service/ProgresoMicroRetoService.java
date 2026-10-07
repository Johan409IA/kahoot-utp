package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.response.ProgresoMicroRetoResponse;

public interface ProgresoMicroRetoService {

	ProgresoMicroRetoResponse mostrarPorUsuarioYReto(Long usuarioId, Long microRetoId);

	List<ProgresoMicroRetoResponse> listarPorUsuario(Long usuarioId);
}
