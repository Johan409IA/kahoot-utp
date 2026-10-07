package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record IntentoRequest(
		@NotNull(message = "El micro-reto es obligatorio.") @Positive(message = "El identificador debe ser positivo.") Long microRetoId,
		boolean acierto,
		boolean usoAyuda,
		@PositiveOrZero(message = "El tiempo no puede ser negativo.") Long tiempoMs) {
}
