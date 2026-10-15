package com.adaptcode.utp.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
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
@Table(name = "salas", indexes = {
		@Index(name = "idx_salas_docente", columnList = "docente_id")
})
public class Sala {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "docente_id", nullable = false)
	private Usuario docente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "quiz_origen_id", nullable = false)
	private Quiz quizOrigen;

	@NotBlank(message = "El PIN es obligatorio")
	@Pattern(regexp = "\\d{6}", message = "El PIN debe tener seis dígitos numéricos")
	@Column(name = "pin", nullable = false, unique = true, length = 6)
	private String pin;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 16)
	private EstadoSala estado = EstadoSala.BORRADOR;

	@Positive(message = "El tiempo por pregunta debe ser positivo")
	@Column(name = "tiempo_por_pregunta", nullable = false)
	private Integer tiempoPorPregunta = 60;

	@Enumerated(EnumType.STRING)
	@Column(name = "ampliacion", nullable = false, length = 16)
	private AmpliacionSala ampliacion = AmpliacionSala.ESTANDAR;

	@Column(name = "quiz_version_congelada_en")
	private Instant quizVersionCongeladaEn;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public Sala(Usuario docente, Quiz quizOrigen, String pin, Integer tiempoPorPregunta) {
		this.docente = docente;
		this.quizOrigen = quizOrigen;
		this.pin = pin;
		this.tiempoPorPregunta = tiempoPorPregunta;
		this.estado = EstadoSala.BORRADOR;
		this.ampliacion = AmpliacionSala.ESTANDAR;
	}

	@PrePersist
	private void fijarFechaCreacion() {
		if (creadoEn == null) {
			creadoEn = Instant.now();
		}
	}
}
