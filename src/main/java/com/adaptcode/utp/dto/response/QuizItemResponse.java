package com.adaptcode.utp.dto.response;

public record QuizItemResponse(
		Long id,
		Long preguntaId,
		String enunciado,
		Integer orden) {
}
