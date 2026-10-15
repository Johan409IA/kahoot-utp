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
import jakarta.validation.constraints.NotNull;
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
@Table(name = "quiz_items", uniqueConstraints = {
		@UniqueConstraint(name = "uk_quiz_items_quiz_orden", columnNames = { "quiz_id", "orden" }),
		@UniqueConstraint(name = "uk_quiz_items_quiz_pregunta", columnNames = { "quiz_id", "pregunta_id" })
})
public class QuizItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "quiz_id", nullable = false)
	private Quiz quiz;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "pregunta_id", nullable = false)
	private PreguntaDocente pregunta;

	@NotNull(message = "El orden es obligatorio")
	@Positive(message = "El orden debe ser positivo")
	@Column(name = "orden", nullable = false)
	private Integer orden;

	public QuizItem(Quiz quiz, PreguntaDocente pregunta, Integer orden) {
		this.quiz = quiz;
		this.pregunta = pregunta;
		this.orden = orden;
	}
}
