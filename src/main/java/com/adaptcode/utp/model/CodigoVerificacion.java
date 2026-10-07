package com.adaptcode.utp.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "codigos_verificacion", indexes = {
		@Index(name = "idx_codigos_correo", columnList = "correo")
})
public class CodigoVerificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El correo es obligatorio")
	@Email(message = "El formato del correo no es válido")
	@Size(max = 254, message = "El correo no debe superar los 254 caracteres")
	@Column(name = "correo", nullable = false, length = 254)
	private String correo;

	@NotBlank(message = "El código es obligatorio")
	@Pattern(regexp = "\\d{6}", message = "El código debe tener seis dígitos numéricos")
	@Column(name = "codigo", nullable = false, length = 6)
	private String codigo;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 32)
	private TipoCodigoVerificacion tipo;

	@NotNull(message = "La expiración es obligatoria")
	@Column(name = "expira_en", nullable = false)
	private Instant expiraEn;

	@Column(name = "usado", nullable = false)
	private boolean usado;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public CodigoVerificacion(String correo, String codigo, TipoCodigoVerificacion tipo,
			Instant expiraEn) {
		this.correo = correo;
		this.codigo = codigo;
		this.tipo = tipo;
		this.expiraEn = expiraEn;
	}

	@PrePersist
	private void fijarFechaCreacion() {
		if (creadoEn == null) {
			creadoEn = Instant.now();
		}
	}
}
