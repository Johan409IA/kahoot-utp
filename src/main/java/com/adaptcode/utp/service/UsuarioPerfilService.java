package com.adaptcode.utp.service;

import com.adaptcode.utp.dto.request.UsuarioPerfilRequest;
import com.adaptcode.utp.dto.response.UsuarioPerfilResponse;

public interface UsuarioPerfilService {

	UsuarioPerfilResponse completar(Long usuarioId, UsuarioPerfilRequest request);
}
