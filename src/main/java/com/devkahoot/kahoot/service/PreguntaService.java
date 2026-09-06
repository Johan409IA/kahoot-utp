package com.devkahoot.kahoot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.devkahoot.kahoot.model.Pregunta;

@Service
public class PreguntaService {

	private final List<Pregunta> preguntas = new ArrayList<>();
	private int siguienteId = 1;

	public List<Pregunta> obtenerTodasLasPreguntas() {
		return preguntas;
	}

	public Pregunta obtenerPreguntaPorId(int id) {
		return preguntas.stream()
				.filter(p -> p.getId() == id)
				.findFirst()
				.orElse(null);
	}

	public Pregunta agregarNuevaPregunta(Pregunta pregunta) {
		if (!esPreguntaValida(pregunta)) {
			return null;
		}
		pregunta.setId(siguienteId++);
		pregunta.setOrden(calcularSiguienteOrden());
		preguntas.add(pregunta);
		return pregunta;
	}

	public Pregunta actualizarPregunta(int id, Pregunta nuevaPregunta) {
		if (!esPreguntaValida(nuevaPregunta)) {
			return null;
		}
		Pregunta existente = obtenerPreguntaPorId(id);
		if (existente == null) {
			return null;
		}
		int posicion = preguntas.indexOf(existente);
		nuevaPregunta.setId(id);
		nuevaPregunta.setOrden(existente.getOrden());
		preguntas.set(posicion, nuevaPregunta);
		return nuevaPregunta;
	}

	public boolean eliminarPregunta(int id) {
		return preguntas.removeIf(p -> p.getId() == id);
	}

	public boolean esPreguntaValida(Pregunta pregunta) {
		return pregunta != null
				&& pregunta.getEnunciado() != null && !pregunta.getEnunciado().isBlank()
				&& pregunta.getOpciones() != null && pregunta.getOpciones().size() >= 2
				&& pregunta.getRespuestaCorrecta() != null && !pregunta.getRespuestaCorrecta().isBlank()
				&& pregunta.getOpciones().contains(pregunta.getRespuestaCorrecta());
	}

	private int calcularSiguienteOrden() {
		return preguntas.stream()
				.mapToInt(Pregunta::getOrden)
				.max()
				.orElse(0) + 1;
	}
}
