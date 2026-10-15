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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(name = "anulaciones_pregunta")
public class AnulacionPregunta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "sala_id", nullable = false)
	private Sala sala;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "pregunta_id", nullable = false)
	private PreguntaDocente pregunta;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "docente_id", nullable = false)
	private Usuario docente;

	@NotBlank(message = "El motivo es obligatorio")
	@Size(max = 500, message = "El motivo no debe superar los 500 caracteres")
	@Column(name = "motivo", nullable = false, length = 500)
	private String motivo;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public AnulacionPregunta(Sala sala, PreguntaDocente pregunta, Usuario docente,
			String motivo) {
		this.sala = sala;
		this.pregunta = pregunta;
		this.docente = docente;
		this.motivo = motivo;
	}

	@PrePersist
	private void fijarFechaCreacion() {
		if (creadoEn == null) {
			creadoEn = Instant.now();
		}
	}
}
