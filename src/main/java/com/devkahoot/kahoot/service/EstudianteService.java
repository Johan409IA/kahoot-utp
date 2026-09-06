package com.devkahoot.kahoot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.devkahoot.kahoot.model.Estudiante;

@Service
public class EstudianteService {

	private final List<Estudiante> estudiantes = new ArrayList<>();
	private int siguienteId = 1;

	public List<Estudiante> obtenerTodosLosEstudiantes() {
		return estudiantes;
	}

	public Estudiante obtenerEstudiantePorId(int id) {
		return estudiantes.stream()
				.filter(e -> e.getId() == id)
				.findFirst()
				.orElse(null);
	}

	public Estudiante agregarEstudiante(Estudiante estudiante) {
		if (estudiante == null || estudiante.getApodo() == null || estudiante.getApodo().isBlank()) {
			return null;
		}
		estudiante.setId(siguienteId++);
		estudiantes.add(estudiante);
		return estudiante;
	}

	public Estudiante actualizarEstudiante(int id, Estudiante estudiante) {
		if (estudiante == null || estudiante.getApodo() == null || estudiante.getApodo().isBlank()) {
			return null;
		}
		Estudiante existente = obtenerEstudiantePorId(id);
		if (existente == null) {
			return null;
		}
		existente.setApodo(estudiante.getApodo());
		existente.setCodigoPartida(estudiante.getCodigoPartida());
		existente.setPuntaje(estudiante.getPuntaje());
		return existente;
	}

	public boolean eliminarEstudiante(int id) {
		return estudiantes.removeIf(e -> e.getId() == id);
	}
}
