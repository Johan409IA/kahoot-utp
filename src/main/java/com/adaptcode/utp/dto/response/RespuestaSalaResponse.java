package com.adaptcode.utp.dto.response;

public record RespuestaSalaResponse(
		Long id,
		Long participacionId,
		Long preguntaId,
		boolean acierto,
		boolean omitida,
		Long tiempoMs) {
}
