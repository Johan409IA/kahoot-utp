package com.adaptcode.utp.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios", indexes = {
		@Index(name = "idx_usuarios_rol", columnList = "rol")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "correo", nullable = false, unique = true, length = 254)
	private String correo;

	@Column(name = "nombres", nullable = false, length = 100)
	private String nombres;

	@Column(name = "apellidos", nullable = false, length = 100)
	private String apellidos;

	@Column(name = "contrasena_hash", nullable = false)
	private String contrasenaHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "rol", nullable = false, length = 32)
	private RolUsuario rol;

	// Obligatorio en el registro (validado en la capa de solicitud); la columna
	// admite nulos para no romper cuentas creadas antes de este cambio en Supabase.
	@Enumerated(EnumType.STRING)
	@Column(name = "genero", length = 16)
	private GeneroUsuario genero;

	@Column(name = "fecha_nacimiento")
	private LocalDate fechaNacimiento;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "credencial_cambiada_en")
	private Instant credencialCambiadaEn;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public Usuario(String correo, String nombres, String apellidos,
			String contrasenaHash, RolUsuario rol, boolean activo,
			GeneroUsuario genero, LocalDate fechaNacimiento) {
		this.correo = correo;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.contrasenaHash = contrasenaHash;
		this.rol = rol;
		this.activo = activo;
		this.genero = genero;
		this.fechaNacimiento = fechaNacimiento;
	}

	@PrePersist
	@PreUpdate
	private void normalizarCorreoYFecha() {
		if (correo != null) {
			correo = correo.trim().toLowerCase(Locale.ROOT);
		}
		if (creadoEn == null) {
			creadoEn = Instant.now();
		}
	}
}
