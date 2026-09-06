package com.devkahoot.kahoot.controller;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devkahoot.kahoot.model.Estudiante;
import com.devkahoot.kahoot.service.EstudianteService;

@RestController
@RequestMapping("/api")
public class EstudianteController {

	private final EstudianteService estudianteService;

	public EstudianteController(EstudianteService estudianteService) {
		this.estudianteService = estudianteService;
	}

	@GetMapping("/estudiantes")
	public ResponseEntity<List<Estudiante>> obtenerTodosLosEstudiantes() {
		return ResponseEntity.ok(estudianteService.obtenerTodosLosEstudiantes());
	}

	@GetMapping("/estudiantes/{id}")
	public ResponseEntity<Estudiante> obtenerEstudiantePorId(@PathVariable int id) {
		Estudiante estudiante = estudianteService.obtenerEstudiantePorId(id);
		if (estudiante == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(estudiante);
	}

	@PutMapping("/estudiantes/{id}")
	public ResponseEntity<Estudiante> actualizarEstudiante(@PathVariable int id, @RequestBody Estudiante estudiante) {
		Estudiante actualizado = estudianteService.actualizarEstudiante(id, estudiante);
		if (actualizado == null) {
			if (estudianteService.obtenerEstudiantePorId(id) == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(actualizado);
	}

	@DeleteMapping("/estudiantes/{id}")
	public ResponseEntity<Void> eliminarEstudiante(@PathVariable int id) {
		boolean eliminado = estudianteService.eliminarEstudiante(id);
		if (!eliminado) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}
}
