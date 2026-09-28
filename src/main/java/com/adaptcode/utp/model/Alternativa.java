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
@Table(name = "alternativas_micro_reto")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alternativa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "micro_reto_id", nullable = false)
	private MicroReto microReto;

	@Column(name = "texto", length = 1000)
	private String texto;

	@Column(name = "codigo", length = 4000)
	private String codigo;

	@Column(name = "lenguaje", length = 32)
	private String lenguaje;

	@Column(name = "orden_visual", nullable = false)
	private int ordenVisual;

	@Column(name = "correcta", nullable = false)
	private boolean correcta;

	public Alternativa(String texto, String codigo, String lenguaje, int ordenVisual, boolean correcta) {
		this.texto = texto;
		this.codigo = codigo;
		this.lenguaje = lenguaje;
		this.ordenVisual = ordenVisual;
		this.correcta = correcta;
	}

	void asociarA(MicroReto microReto) {
		this.microReto = microReto;
	}
}
