package com.adaptcode.utp.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "micro_retos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MicroReto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "codigo", nullable = false, unique = true, length = 40)
	private String codigo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tema_id", nullable = false)
	private Tema tema;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 32)
	private TipoMicroReto tipo;

	@Enumerated(EnumType.STRING)
	@Column(name = "nivel", nullable = false, length = 16)
	private NivelMicroReto nivel;

	@Column(name = "enunciado", nullable = false, length = 1000)
	private String enunciado;

	@Column(name = "codigo_base", length = 4000)
	private String codigoBase;

	@Column(name = "lenguaje", length = 32)
	private String lenguaje;

	@Column(name = "activo", nullable = false)
	private final boolean activo = true;

	@OneToMany(mappedBy = "microReto", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("ordenVisual ASC")
	private final List<Alternativa> alternativas = new ArrayList<>();

	@OneToMany(mappedBy = "microReto", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("ordenVisual ASC")
	private final List<Bloque> bloques = new ArrayList<>();

	@OneToMany(mappedBy = "microReto", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Retroalimentacion> retroalimentaciones = new ArrayList<>();

	public MicroReto(String codigo, Tema tema, TipoMicroReto tipo, NivelMicroReto nivel,
			String enunciado, String codigoBase, String lenguaje) {
		this.codigo = codigo;
		this.tema = tema;
		this.tipo = tipo;
		this.nivel = nivel;
		this.enunciado = enunciado;
		this.codigoBase = codigoBase;
		this.lenguaje = lenguaje;
	}

	public void agregarAlternativa(Alternativa alternativa) {
		alternativas.add(alternativa);
		alternativa.asociarA(this);
	}

	public void agregarBloque(Bloque bloque) {
		bloques.add(bloque);
		bloque.asociarA(this);
	}

	public void agregarRetroalimentacion(Retroalimentacion retroalimentacion) {
		retroalimentaciones.add(retroalimentacion);
		retroalimentacion.asociarA(this);
	}
}
