package com.devkahoot.kahoot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devkahoot.kahoot.model.EstadoPartida;
import com.devkahoot.kahoot.model.Estudiante;
import com.devkahoot.kahoot.model.Partida;
import com.devkahoot.kahoot.model.PreguntaParaEstudiante;
import com.devkahoot.kahoot.model.Respuesta;
import com.devkahoot.kahoot.model.ResultadoRespuesta;
import com.devkahoot.kahoot.service.PartidaService;

@RestController
@RequestMapping("/api")
public class PartidaController {

	private final PartidaService partidaService;

	public PartidaController(PartidaService partidaService) {
		this.partidaService = partidaService;
	}

	@PostMapping("/partidas")
	public ResponseEntity<Partida> crearNuevaPartida(@RequestBody Partida partida) {
		if (partida == null || partida.getCodigo() == null || partida.getCodigo().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		if (partidaService.obtenerPartidaPorCodigo(partida.getCodigo()) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Partida creada = partidaService.crearNuevaPartida(partida);
		if (creada == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(creada);
	}

	@GetMapping("/partidas/{codigo}")
	public ResponseEntity<Partida> obtenerPartidaPorCodigo(@PathVariable String codigo) {
		Partida partida = partidaService.obtenerPartidaPorCodigo(codigo);
		if (partida == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(partida);
	}

	@PutMapping("/partidas/{codigo}/estado")
	public ResponseEntity<Partida> actualizarEstadoPartida(@PathVariable String codigo,
			@RequestBody EstadoPartida estado) {
		Partida actualizada = partidaService.actualizarEstadoPartida(codigo, estado);
		if (actualizada == null) {
			if (partidaService.obtenerPartidaPorCodigo(codigo) == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(actualizada);
	}

	@GetMapping("/partidas/{codigo}/preguntas")
	public ResponseEntity<List<PreguntaParaEstudiante>> obtenerPreguntasDePartida(@PathVariable String codigo) {
		List<PreguntaParaEstudiante> preguntas = partidaService.obtenerPreguntasDePartida(codigo);
		if (preguntas == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(preguntas);
	}

	@GetMapping("/partidas/{codigo}/estudiantes")
	public ResponseEntity<List<Estudiante>> obtenerEstudiantesDePartida(@PathVariable String codigo) {
		List<Estudiante> estudiantes = partidaService.obtenerEstudiantesDePartida(codigo);
		if (estudiantes == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(estudiantes);
	}

	@PostMapping("/partidas/{codigo}/estudiantes")
	public ResponseEntity<Estudiante> agregarEstudianteAPartida(@PathVariable String codigo,
			@RequestBody Estudiante estudiante) {
		if (estudiante == null || estudiante.getApodo() == null || estudiante.getApodo().isBlank()
				|| estudiante.getCodigoPartida() == null || !codigo.equals(estudiante.getCodigoPartida())) {
			return ResponseEntity.badRequest().build();
		}
		Estudiante registrado = partidaService.agregarEstudianteAPartida(codigo, estudiante);
		if (registrado == null) {
			if (partidaService.obtenerPartidaPorCodigo(codigo) == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(registrado);
	}

	@PostMapping("/partidas/{codigo}/respuestas")
	public ResponseEntity<ResultadoRespuesta> registrarRespuesta(@PathVariable String codigo,
			@RequestBody Respuesta respuesta) {
		if (respuesta == null || respuesta.getOpcionSeleccionada() == null
				|| respuesta.getOpcionSeleccionada().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		ResultadoRespuesta resultado = partidaService.registrarRespuesta(codigo, respuesta);
		if (resultado == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(resultado);
	}
}
