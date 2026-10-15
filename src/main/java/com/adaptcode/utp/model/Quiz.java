package com.adaptcode.utp.model;

import java.time.Instant;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
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
@Table(name = "quizzes", indexes = {
		@Index(name = "idx_quizzes_docente", columnList = "docente_id")
})
public class Quiz {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "docente_id", nullable = false)
	private Usuario docente;

	@NotBlank(message = "El título es obligatorio")
	@Size(min = 3, max = 120, message = "El título debe tener entre 3 y 120 caracteres")
	@Column(name = "titulo", nullable = false, length = 120)
	private String titulo;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 16)
	private EstadoQuiz estado = EstadoQuiz.BORRADOR;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orden ASC")
	private List<QuizItem> items = new ArrayList<>();

	public Quiz(Usuario docente, String titulo) {
		this.docente = docente;
		this.titulo = titulo;
		this.estado = EstadoQuiz.BORRADOR;
	}

	public void agregarItem(QuizItem item) {
		items.add(item);
		item.setQuiz(this);
	}

	@PrePersist
	private void fijarFechaCreacion() {
		if (creadoEn == null) {
			creadoEn = Instant.now();
		}
	}
}
