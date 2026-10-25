package com.adaptcode.utp.dto.request;

import java.time.LocalDate;

import com.adaptcode.utp.model.GeneroUsuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

/**
 * Datos que el usuario completa cuando su cuenta no tiene género ni fecha de
 * nacimiento (por ejemplo, cuentas institucionales creadas antes del cambio).
 */
public record UsuarioPerfilRequest(
		@NotNull(message = "El género es obligatorio.")
		GeneroUsuario genero,

		@NotNull(message = "La fecha de nacimiento es obligatoria.")
		@Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
		LocalDate fechaNacimiento) {
}
