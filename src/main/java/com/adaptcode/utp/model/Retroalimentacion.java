package com.adaptcode.utp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "retroalimentaciones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Retroalimentacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "micro_reto_id", nullable = false)
	private MicroReto microReto;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "alternativa_id")
	private Alternativa alternativa;

	@Column(name = "causa", nullable = false, length = 500)
	private String causa;

	@Column(name = "explicacion", nullable = false, length = 1500)
	private String explicacion;

	@Column(name = "secuencia_textual", nullable = false, length = 1500)
	private String secuenciaTextual;

	@Column(name = "sugerencia", nullable = false, length = 500)
	private String sugerencia;

	public Retroalimentacion(Alternativa alternativa, String causa, String explicacion,
			String secuenciaTextual, String sugerencia) {
		this.alternativa = alternativa;
		this.causa = causa;
		this.explicacion = explicacion;
		this.secuenciaTextual = secuenciaTextual;
		this.sugerencia = sugerencia;
	}

	void asociarA(MicroReto microReto) {
		this.microReto = microReto;
	}
}
