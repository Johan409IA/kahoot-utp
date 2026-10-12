package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificarCodigoRequest(
		@NotBlank(message = "El correo es obligatorio.")
		@Email(message = "El formato del correo no es válido.")
		String correo,

		@NotBlank(message = "El código es obligatorio.")
		@Pattern(regexp = "\\d{6}", message = "El código debe tener seis dígitos numéricos.")
		String codigo) {
}
