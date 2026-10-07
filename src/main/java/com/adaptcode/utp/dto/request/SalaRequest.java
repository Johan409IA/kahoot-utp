package com.adaptcode.utp.dto.request;

import com.adaptcode.utp.model.AmpliacionSala;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SalaRequest(
		@NotNull(message = "El quiz es obligatorio.") @Positive(message = "El identificador debe ser positivo.") Long quizId,
		@NotNull(message = "El tiempo por pregunta es obligatorio.") @Min(value = 20, message = "El tiempo mínimo es 20 segundos.") @Max(value = 120, message = "El tiempo máximo es 120 segundos.") Integer tiempoPorPregunta,
		@NotNull(message = "La ampliación es obligatoria.") AmpliacionSala ampliacion) {
}
