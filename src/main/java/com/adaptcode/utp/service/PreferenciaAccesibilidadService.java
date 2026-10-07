package com.adaptcode.utp.service;

import com.adaptcode.utp.dto.request.PreferenciaAccesibilidadRequest;
import com.adaptcode.utp.dto.response.PreferenciaAccesibilidadResponse;

public interface PreferenciaAccesibilidadService {

	PreferenciaAccesibilidadResponse mostrarPorUsuario(Long usuarioId);

	PreferenciaAccesibilidadResponse guardarPreferencias(
			Long usuarioId, PreferenciaAccesibilidadRequest request);
}
