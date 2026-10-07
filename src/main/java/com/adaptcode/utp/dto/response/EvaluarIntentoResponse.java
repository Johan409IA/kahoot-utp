package com.adaptcode.utp.dto.response;

public record EvaluarIntentoResponse(
		Long intentoId,
		Integer nroIntento,
		boolean acierto,
		RetroalimentacionExplicadaResponse retroalimentacion,
		boolean verSolucionDisponible,
		SolucionExplicadaResponse solucion) {
}
