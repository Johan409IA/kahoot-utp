package com.adaptcode.utp.dto.request;

import com.adaptcode.utp.model.NivelMicroReto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MicroRetoFiltroRequest(
		@NotNull(message = "El curso es obligatorio.") @Positive(message = "El identificador debe ser positivo.") Long cursoId,
		@Positive(message = "El identificador debe ser positivo.") Long temaId,
		NivelMicroReto nivel) {
}
