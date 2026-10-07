package com.adaptcode.utp.dto.response;

import java.util.List;

import com.adaptcode.utp.model.EstadoQuiz;

public record QuizResponse(
		Long id,
		Long docenteId,
		String titulo,
		EstadoQuiz estado,
		List<QuizItemResponse> items) {
}
