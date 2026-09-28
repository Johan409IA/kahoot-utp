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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "temas", uniqueConstraints = @UniqueConstraint(
		name = "uk_temas_curso_nombre", columnNames = { "curso_id", "nombre" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tema {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id", nullable = false)
	private Curso curso;

	@Column(name = "nombre", nullable = false, length = 120)
	private String nombre;

	@Column(name = "activo", nullable = false)
	private final boolean activo = true;

	public Tema(Curso curso, String nombre) {
		this.curso = curso;
		this.nombre = nombre;
	}
}
