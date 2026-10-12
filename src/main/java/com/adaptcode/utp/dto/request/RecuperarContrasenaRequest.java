package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarContrasenaRequest(
		@NotBlank(message = "El correo es obligatorio.")
		@Email(message = "El formato del correo no es válido.")
		String correo) {
}
