package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AnulacionPreguntaRequest(
		@NotNull(message = "La pregunta es obligatoria.") @Positive(message = "El identificador debe ser positivo.") Long preguntaId,
		@NotBlank(message = "El motivo es obligatorio.") @Size(max = 500, message = "El motivo no debe superar los 500 caracteres.") String motivo) {
}
