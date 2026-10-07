package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ParticipacionSalaRequest(
		@NotNull(message = "El estudiante es obligatorio.") @Positive(message = "El identificador debe ser positivo.") Long estudianteId) {
}
