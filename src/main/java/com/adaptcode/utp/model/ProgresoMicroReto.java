package com.adaptcode.utp.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "progreso_micro_retos", uniqueConstraints = {
		@UniqueConstraint(name = "uk_progreso_usuario_reto", columnNames = {
				"usuario_id", "micro_reto_id" })
})
public class ProgresoMicroReto {

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

	@Column(name = "completado", nullable = false)
	private boolean completado;

	@Column(name = "resuelto_con_ayuda", nullable = false)
	private boolean resueltoConAyuda;

	@Column(name = "punto_dominio", nullable = false)
	private boolean puntoDominio;

	@Column(name = "actualizado_en", nullable = false)
	private Instant actualizadoEn;

	public ProgresoMicroReto(Usuario usuario, MicroReto microReto) {
		this.usuario = usuario;
		this.microReto = microReto;
	}

	@PrePersist
	@PreUpdate
	private void fijarFechaActualizacion() {
		actualizadoEn = Instant.now();
	}
}
