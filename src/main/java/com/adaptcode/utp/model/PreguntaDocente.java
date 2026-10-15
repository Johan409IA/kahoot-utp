package com.adaptcode.utp.model;

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
@Table(name = "preguntas_docente", indexes = {
		@Index(name = "idx_preguntas_docente_docente", columnList = "docente_id")
})
public class PreguntaDocente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "docente_id", nullable = false)
	private Usuario docente;

	@Enumerated(EnumType.STRING)
	@Column(name = "origen", nullable = false, length = 16)
	private OrigenPregunta origen;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "micro_reto_origen_id")
	private MicroReto microRetoOrigen;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "tema_id", nullable = false)
	private Tema tema;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 32)
	private TipoMicroReto tipo;

	@Enumerated(EnumType.STRING)
	@Column(name = "nivel", nullable = false, length = 16)
	private NivelMicroReto nivel;

	@NotBlank(message = "El enunciado es obligatorio")
	@Size(max = 1000, message = "El enunciado no debe superar los 1000 caracteres")
	@Column(name = "enunciado", nullable = false, length = 1000)
	private String enunciado;

	@Column(name = "activa", nullable = false)
	private boolean activa = true;

	public PreguntaDocente(Usuario docente, OrigenPregunta origen, Tema tema,
			TipoMicroReto tipo, NivelMicroReto nivel, String enunciado) {
		this.docente = docente;
		this.origen = origen;
		this.tema = tema;
		this.tipo = tipo;
		this.nivel = nivel;
		this.enunciado = enunciado;
	}

	public void marcarOrigenInstitucional(MicroReto microReto) {
		this.microRetoOrigen = microReto;
	}
}
