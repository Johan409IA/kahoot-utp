package com.adaptcode.utp.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuizRequest(
		@NotBlank(message = "El título es obligatorio.") @Size(min = 3, max = 120, message = "El título debe tener entre 3 y 120 caracteres.") String titulo,
		@NotNull(message = "Los ítems son obligatorios.") List<@Valid QuizItemRequest> items) {
}
