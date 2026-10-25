package com.adaptcode.utp.dto.request;

import java.time.LocalDate;

import com.adaptcode.utp.model.GeneroUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
		@NotBlank(message = "Los nombres son obligatorios.")
		@Size(max = 100, message = "Los nombres no deben superar los 100 caracteres.")
		String nombres,

		@NotBlank(message = "Los apellidos son obligatorios.")
		@Size(max = 100, message = "Los apellidos no deben superar los 100 caracteres.")
		String apellidos,

		@NotBlank(message = "El correo es obligatorio.")
		@Email(message = "El formato del correo no es válido.")
		@Size(max = 254, message = "El correo no debe superar los 254 caracteres.")
		String correo,

		@NotBlank(message = "La contraseña es obligatoria.")
		@Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
		String contrasena,

		@NotNull(message = "El género es obligatorio.")
		GeneroUsuario genero,

		@NotNull(message = "La fecha de nacimiento es obligatoria.")
		@Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
		LocalDate fechaNacimiento) {
}
