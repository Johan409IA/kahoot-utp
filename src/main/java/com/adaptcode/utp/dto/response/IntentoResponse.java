package com.adaptcode.utp.dto.response;

public record IntentoResponse(
		Long id,
		Long usuarioId,
		Long microRetoId,
		Integer nroIntento,
		boolean acierto,
		boolean usoAyuda,
		Long tiempoMs) {
}
