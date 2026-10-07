package com.adaptcode.utp.dto.response;

public record RetroalimentacionDocenteResponse(
		Long id,
		Long preguntaId,
		Long alternativaId,
		String causa,
		String explicacion,
		String secuenciaTextual,
		String sugerencia) {
}
