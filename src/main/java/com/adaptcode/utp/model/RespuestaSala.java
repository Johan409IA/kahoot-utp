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
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "respuestas_sala", uniqueConstraints = {
		@UniqueConstraint(name = "uk_respuestas_participacion_pregunta", columnNames = {
				"participacion_id", "pregunta_id" })
})
public class RespuestaSala {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "participacion_id", nullable = false)
	private ParticipacionSala participacion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "pregunta_id", nullable = false)
	private PreguntaDocente pregunta;

	@Column(name = "acierto", nullable = false)
	private boolean acierto;

	@Column(name = "omitida", nullable = false)
	private boolean omitida;

	@PositiveOrZero(message = "El tiempo no puede ser negativo")
	@Column(name = "tiempo_ms")
	private Long tiempoMs;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public RespuestaSala(ParticipacionSala participacion, PreguntaDocente pregunta,
			boolean acierto, boolean omitida) {
		this.participacion = participacion;
		this.pregunta = pregunta;
		this.acierto = acierto;
		this.omitida = omitida;
	}

	@PrePersist
	private void fijarFechaCreacion() {
		if (creadoEn == null) {
			creadoEn = Instant.now();
		}
	}
}
