package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RetroalimentacionDocenteRequest(
		@NotNull(message = "La alternativa es obligatoria.") @Positive(message = "El identificador debe ser positivo.") Long alternativaId,
		@NotBlank(message = "La causa es obligatoria.") @Size(max = 500, message = "La causa no debe superar los 500 caracteres.") String causa,
		@NotBlank(message = "La explicación es obligatoria.") @Size(max = 1500, message = "La explicación no debe superar los 1500 caracteres.") String explicacion,
		@NotBlank(message = "La secuencia textual es obligatoria.") @Size(max = 1500, message = "La secuencia no debe superar los 1500 caracteres.") String secuenciaTextual,
		@NotBlank(message = "La sugerencia es obligatoria.") @Size(max = 500, message = "La sugerencia no debe superar los 500 caracteres.") String sugerencia) {
}
