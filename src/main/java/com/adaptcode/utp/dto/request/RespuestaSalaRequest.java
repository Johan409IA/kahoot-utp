package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RespuestaSalaRequest(
		@NotNull(message = "La participación es obligatoria.") @Positive(message = "El identificador debe ser positivo.") Long participacionId,
		@NotNull(message = "La pregunta es obligatoria.") @Positive(message = "El identificador debe ser positivo.") Long preguntaId,
		boolean acierto,
		boolean omitida,
		@PositiveOrZero(message = "El tiempo no puede ser negativo.") Long tiempoMs) {
}
