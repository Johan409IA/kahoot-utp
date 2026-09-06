package com.devkahoot.kahoot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devkahoot.kahoot.model.Pregunta;
import com.devkahoot.kahoot.service.PartidaService;
import com.devkahoot.kahoot.service.PreguntaService;

@RestController
@RequestMapping("/api")
public class PreguntaController {

	private final PreguntaService preguntaService;
	private final PartidaService partidaService;

	public PreguntaController(PreguntaService preguntaService, PartidaService partidaService) {
		this.preguntaService = preguntaService;
		this.partidaService = partidaService;
	}

	@GetMapping("/preguntas")
	public ResponseEntity<List<Pregunta>> obtenerTodasLasPreguntas() {
		return ResponseEntity.ok(preguntaService.obtenerTodasLasPreguntas());
	}

	@GetMapping("/preguntas/{id}")
	public ResponseEntity<Pregunta> obtenerPreguntaPorId(@PathVariable int id) {
		Pregunta pregunta = preguntaService.obtenerPreguntaPorId(id);
		if (pregunta == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(pregunta);
	}

	@PostMapping("/preguntas")
	public ResponseEntity<Pregunta> agregarNuevaPregunta(@RequestBody Pregunta pregunta) {
		Pregunta guardada = preguntaService.agregarNuevaPregunta(pregunta);
		if (guardada == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
	}

	@PutMapping("/preguntas/{id}")
	public ResponseEntity<Pregunta> actualizarPregunta(@PathVariable int id, @RequestBody Pregunta nuevaPregunta) {
		Pregunta actualizada = preguntaService.actualizarPregunta(id, nuevaPregunta);
		if (actualizada == null) {
			if (preguntaService.obtenerPreguntaPorId(id) == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(actualizada);
	}

	@DeleteMapping("/preguntas/{id}")
	public ResponseEntity<Void> eliminarPregunta(@PathVariable int id) {
		if (partidaService.estaPreguntaEnUso(id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		boolean eliminada = preguntaService.eliminarPregunta(id);
		if (!eliminada) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}
}
