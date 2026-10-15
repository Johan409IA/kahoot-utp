package com.adaptcode.utp.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "intentos", uniqueConstraints = {
		@UniqueConstraint(name = "uk_intentos_usuario_reto_nro", columnNames = {
				"usuario_id", "micro_reto_id", "nro_intento" })
}, indexes = {
		@Index(name = "idx_intentos_usuario_reto", columnList = "usuario_id, micro_reto_id")
})
public class Intento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "micro_reto_id", nullable = false)
	private MicroReto microReto;

	@NotNull(message = "El número de intento es obligatorio")
	@Positive(message = "El número de intento debe ser positivo")
	@Column(name = "nro_intento", nullable = false)
	private Integer nroIntento;

	@Column(name = "acierto", nullable = false)
	private boolean acierto;

	@Column(name = "uso_ayuda", nullable = false)
	private boolean usoAyuda;

	@PositiveOrZero(message = "El tiempo no puede ser negativo")
	@Column(name = "tiempo_ms")
	private Long tiempoMs;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public Intento(Usuario usuario, MicroReto microReto, Integer nroIntento, boolean acierto,
			boolean usoAyuda) {
		this.usuario = usuario;
		this.microReto = microReto;
		this.nroIntento = nroIntento;
		this.acierto = acierto;
		this.usoAyuda = usoAyuda;
	}

	@PrePersist
	private void fijarFechaCreacion() {
		if (creadoEn == null) {
			creadoEn = Instant.now();
		}
	}
}
