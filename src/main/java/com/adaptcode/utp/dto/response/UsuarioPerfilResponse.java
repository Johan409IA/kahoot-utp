package com.adaptcode.utp.dto.response;

import java.time.LocalDate;

import com.adaptcode.utp.model.GeneroUsuario;

public record UsuarioPerfilResponse(
		Long usuarioId,
		GeneroUsuario genero,
		LocalDate fechaNacimiento) {
}
