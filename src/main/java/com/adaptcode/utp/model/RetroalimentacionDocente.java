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
@Table(name = "retroalimentaciones_docente", uniqueConstraints = {
		@UniqueConstraint(name = "uk_retro_docente_alternativa", columnNames = {
				"alternativa_id" })
})
public class RetroalimentacionDocente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "pregunta_id", nullable = false)
	private PreguntaDocente pregunta;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "alternativa_id", nullable = false)
	private AlternativaDocente alternativa;

	@NotBlank(message = "La causa es obligatoria")
	@Size(max = 500, message = "La causa no debe superar los 500 caracteres")
	@Column(name = "causa", nullable = false, length = 500)
	private String causa;

	@NotBlank(message = "La explicación es obligatoria")
	@Size(max = 1500, message = "La explicación no debe superar los 1500 caracteres")
	@Column(name = "explicacion", nullable = false, length = 1500)
	private String explicacion;

	@NotBlank(message = "La secuencia textual es obligatoria")
	@Size(max = 1500, message = "La secuencia no debe superar los 1500 caracteres")
	@Column(name = "secuencia_textual", nullable = false, length = 1500)
	private String secuenciaTextual;

	@NotBlank(message = "La sugerencia es obligatoria")
	@Size(max = 500, message = "La sugerencia no debe superar los 500 caracteres")
	@Column(name = "sugerencia", nullable = false, length = 500)
	private String sugerencia;

	public RetroalimentacionDocente(PreguntaDocente pregunta, AlternativaDocente alternativa,
			String causa, String explicacion, String secuenciaTextual, String sugerencia) {
		this.pregunta = pregunta;
		this.alternativa = alternativa;
		this.causa = causa;
		this.explicacion = explicacion;
		this.secuenciaTextual = secuenciaTextual;
		this.sugerencia = sugerencia;
	}
}
