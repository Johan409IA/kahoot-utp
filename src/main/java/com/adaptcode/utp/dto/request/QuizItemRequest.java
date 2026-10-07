package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuizItemRequest(
		@NotNull(message = "La pregunta es obligatoria.") @Positive(message = "El identificador debe ser positivo.") Long preguntaId,
		@NotNull(message = "El orden es obligatorio.") @Positive(message = "El orden debe ser positivo.") Integer orden) {
}
