package com.adaptcode.utp.dto.response;

public record AnulacionPreguntaResponse(
		Long id,
		Long salaId,
		Long preguntaId,
		Long docenteId,
		String motivo) {
}
