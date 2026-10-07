package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AlternativaDocenteRequest(
		@Size(max = 1000, message = "El texto no debe superar los 1000 caracteres.") String texto,
		@Size(max = 4000, message = "El código no debe superar los 4000 caracteres.") String codigo,
		@Size(max = 32, message = "El lenguaje no debe superar los 32 caracteres.") String lenguaje,
		@PositiveOrZero(message = "El orden no puede ser negativo.") int ordenVisual,
		boolean correcta) {
}
