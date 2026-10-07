package com.adaptcode.utp.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record EvaluarIntentoRequest(
		@NotNull(message = "El micro-reto es obligatorio.") @Positive(message = "El identificador debe ser positivo.") Long microRetoId,
		@Positive(message = "El identificador debe ser positivo.") Long alternativaId,
		List<Long> ordenBloques,
		boolean usoAyuda,
		@PositiveOrZero(message = "El tiempo no puede ser negativo.") Long tiempoMs) {
}
