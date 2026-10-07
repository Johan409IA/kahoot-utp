package com.adaptcode.utp.dto.response;

public record ProgresoMicroRetoResponse(
		Long id,
		Long usuarioId,
		Long microRetoId,
		boolean completado,
		boolean resueltoConAyuda,
		boolean puntoDominio) {
}
