package com.adaptcode.utp.dto.response;

import com.adaptcode.utp.model.RolUsuario;

public record AuthResponse(
		String token,
		String tipoToken,
		long expiraEnMs,
		Long usuarioId,
		String correo,
		String nombres,
		String apellidos,
		RolUsuario rol) {
}
