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
@Table(name = "alternativas_docente", uniqueConstraints = {
		@UniqueConstraint(name = "uk_alternativas_docente_pregunta_orden", columnNames = {
				"pregunta_id", "orden_visual" })
})
public class AlternativaDocente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "pregunta_id", nullable = false)
	private PreguntaDocente pregunta;

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

	public AlternativaDocente(PreguntaDocente pregunta, String texto, String codigo,
			String lenguaje, int ordenVisual, boolean correcta) {
		this.pregunta = pregunta;
		this.texto = texto;
		this.codigo = codigo;
		this.lenguaje = lenguaje;
		this.ordenVisual = ordenVisual;
		this.correcta = correcta;
	}
}
