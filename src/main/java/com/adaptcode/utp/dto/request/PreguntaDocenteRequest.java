package com.adaptcode.utp.dto.request;

import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.TipoMicroReto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PreguntaDocenteRequest(
		@NotNull(message = "El tema es obligatorio.") @Positive(message = "El identificador debe ser positivo.") Long temaId,
		@NotNull(message = "El tipo es obligatorio.") TipoMicroReto tipo,
		@NotNull(message = "El nivel es obligatorio.") NivelMicroReto nivel,
		@NotBlank(message = "El enunciado es obligatorio.") @Size(max = 1000, message = "El enunciado no debe superar los 1000 caracteres.") String enunciado) {
}
