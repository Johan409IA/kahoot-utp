package com.adaptcode.utp.dto.response;

import java.time.Instant;

import com.adaptcode.utp.model.RolUsuario;

public record RegistroResponse(
		Long usuarioId,
		String correo,
		RolUsuario rol,
		boolean requiereVerificacion,
		String codigoVerificacion,
		Instant expiraEn) {
}
