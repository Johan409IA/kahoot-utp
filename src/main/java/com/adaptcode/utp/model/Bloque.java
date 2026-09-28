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
@Table(name = "bloques_micro_reto")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bloque {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "micro_reto_id", nullable = false)
	private MicroReto microReto;

	@Column(name = "texto", nullable = false, length = 1000)
	private String texto;

	@Column(name = "orden_visual", nullable = false)
	private int ordenVisual;

	@Column(name = "orden_correcto", nullable = false)
	private int ordenCorrecto;

	public Bloque(String texto, int ordenVisual, int ordenCorrecto) {
		this.texto = texto;
		this.ordenVisual = ordenVisual;
		this.ordenCorrecto = ordenCorrecto;
	}

	void asociarA(MicroReto microReto) {
		this.microReto = microReto;
	}
}
