package com.devkahoot.kahoot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.devkahoot.kahoot.model.Estudiante;
import com.devkahoot.kahoot.model.EstadoPartida;
import com.devkahoot.kahoot.model.Partida;
import com.devkahoot.kahoot.model.Pregunta;
import com.devkahoot.kahoot.model.PreguntaParaEstudiante;
import com.devkahoot.kahoot.model.Respuesta;
import com.devkahoot.kahoot.model.ResultadoRespuesta;

@Service
public class PartidaService {

	private static final int PUNTOS_RESPUESTA_CORRECTA = 100;

	private final List<Partida> partidas = new ArrayList<>();
	private final List<Respuesta> respuestas = new ArrayList<>();

	private final PreguntaService preguntaService;
	private final EstudianteService estudianteService;

	public PartidaService(PreguntaService preguntaService, EstudianteService estudianteService) {
		this.preguntaService = preguntaService;
		this.estudianteService = estudianteService;
	}

	public Partida crearNuevaPartida(Partida partida) {
		if (!esPartidaValida(partida) || obtenerPartidaPorCodigo(partida.getCodigo()) != null) {
			return null;
		}
		partida.setEstado(EstadoPartida.ESPERANDO);
		partidas.add(partida);
		return partida;
	}

	public Partida obtenerPartidaPorCodigo(String codigo) {
		return partidas.stream()
				.filter(p -> p.getCodigo().equals(codigo))
				.findFirst()
				.orElse(null);
	}

	public Partida actualizarEstadoPartida(String codigo, EstadoPartida estado) {
		if (estado == null) {
			return null;
		}
		Partida partida = obtenerPartidaPorCodigo(codigo);
		if (partida == null) {
			return null;
		}
		partida.setEstado(estado);
		return partida;
	}

	public List<PreguntaParaEstudiante> obtenerPreguntasDePartida(String codigo) {
		Partida partida = obtenerPartidaPorCodigo(codigo);
		if (partida == null) {
			return null;
		}
		List<PreguntaParaEstudiante> preguntas = new ArrayList<>();
		for (Integer idPregunta : partida.getIdsPreguntas()) {
			Pregunta pregunta = preguntaService.obtenerPreguntaPorId(idPregunta);
			if (pregunta != null) {
				preguntas.add(convertirAPreguntaPublica(pregunta));
			}
		}
		return preguntas;
	}

	public List<Estudiante> obtenerEstudiantesDePartida(String codigo) {
		Partida partida = obtenerPartidaPorCodigo(codigo);
		if (partida == null) {
			return null;
		}
		List<Estudiante> estudiantes = new ArrayList<>();
		for (Integer idEstudiante : partida.getIdsEstudiantes()) {
			Estudiante estudiante = estudianteService.obtenerEstudiantePorId(idEstudiante);
			if (estudiante != null) {
				estudiantes.add(estudiante);
			}
		}
		return estudiantes;
	}

	public Estudiante agregarEstudianteAPartida(String codigo, Estudiante estudiante) {
		if (estudiante == null || estudiante.getApodo() == null || estudiante.getApodo().isBlank()
				|| estudiante.getCodigoPartida() == null || !codigo.equals(estudiante.getCodigoPartida())) {
			return null;
		}
		Partida partida = obtenerPartidaPorCodigo(codigo);
		if (partida == null || partida.getEstado() == EstadoPartida.FINALIZADA) {
			return null;
		}
		if (existeApodoEnPartida(partida, estudiante.getApodo())) {
			return null;
		}
		estudiante.setCodigoPartida(codigo);
		Estudiante registrado = estudianteService.agregarEstudiante(estudiante);
		if (registrado == null) {
			return null;
		}
		partida.getIdsEstudiantes().add(registrado.getId());
		return registrado;
	}

	public ResultadoRespuesta registrarRespuesta(String codigo, Respuesta respuesta) {
		if (respuesta == null || respuesta.getOpcionSeleccionada() == null
				|| respuesta.getOpcionSeleccionada().isBlank()) {
			return null;
		}
		Partida partida = obtenerPartidaPorCodigo(codigo);
		if (partida == null || partida.getEstado() != EstadoPartida.EN_CURSO) {
			return null;
		}
		if (!partida.getIdsEstudiantes().contains(respuesta.getEstudianteId())
				|| !partida.getIdsPreguntas().contains(respuesta.getPreguntaId())) {
			return null;
		}
		Estudiante estudiante = estudianteService.obtenerEstudiantePorId(respuesta.getEstudianteId());
		Pregunta pregunta = preguntaService.obtenerPreguntaPorId(respuesta.getPreguntaId());
		if (estudiante == null || pregunta == null) {
			return null;
		}
		if (existeRespuestaDeEstudiante(respuesta.getEstudianteId(), respuesta.getPreguntaId())) {
			throw new RespuestaDuplicadaException(
					"El estudiante ya respondió esta pregunta en la partida " + codigo + ".");
		}
		respuestas.add(respuesta);
		boolean correcta = pregunta.getRespuestaCorrecta().equals(respuesta.getOpcionSeleccionada());
		int puntos = correcta ? PUNTOS_RESPUESTA_CORRECTA : 0;
		estudiante.setPuntaje(estudiante.getPuntaje() + puntos);
		String retroalimentacion = (correcta ? "Correcta. " : "Incorrecta. ")
				+ (pregunta.getExplicacion() != null ? pregunta.getExplicacion() : "");
		return new ResultadoRespuesta(correcta, puntos, retroalimentacion);
	}

	public boolean estaPreguntaEnUso(int idPregunta) {
		return partidas.stream()
				.anyMatch(partida -> partida.getIdsPreguntas().contains(idPregunta));
	}

	private boolean esPartidaValida(Partida partida) {
		return partida != null
				&& partida.getCodigo() != null && !partida.getCodigo().isBlank()
				&& partida.getNombre() != null && !partida.getNombre().isBlank()
				&& partida.getIdsPreguntas() != null && !partida.getIdsPreguntas().isEmpty()
				&& partida.getIdsPreguntas().stream()
						.allMatch(idPregunta -> idPregunta != null && preguntaService.obtenerPreguntaPorId(idPregunta) != null);
	}

	private boolean existeApodoEnPartida(Partida partida, String apodo) {
		return obtenerEstudiantesDePartida(partida.getCodigo()).stream()
				.anyMatch(e -> e.getApodo().equalsIgnoreCase(apodo));
	}

	private boolean existeRespuestaDeEstudiante(int estudianteId, int preguntaId) {
		return respuestas.stream()
				.anyMatch(r -> r.getEstudianteId() == estudianteId && r.getPreguntaId() == preguntaId);
	}

	private PreguntaParaEstudiante convertirAPreguntaPublica(Pregunta pregunta) {
		return new PreguntaParaEstudiante(pregunta.getId(), pregunta.getEnunciado(),
				pregunta.getOpciones(), pregunta.getCategoria(), pregunta.getOrden());
	}
}
