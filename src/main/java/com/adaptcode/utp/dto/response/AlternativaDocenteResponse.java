package com.adaptcode.utp.dto.response;

public record AlternativaDocenteResponse(
		Long id,
		Long preguntaId,
		String texto,
		String codigo,
		String lenguaje,
		int ordenVisual,
		boolean correcta) {
}
