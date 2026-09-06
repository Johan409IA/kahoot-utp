package com.devkahoot.kahoot;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.devkahoot.kahoot.model.EstadoPartida;
import com.devkahoot.kahoot.model.Estudiante;
import com.devkahoot.kahoot.model.Partida;
import com.devkahoot.kahoot.model.Pregunta;
import com.devkahoot.kahoot.model.Respuesta;
import com.devkahoot.kahoot.model.ResultadoRespuesta;
import com.devkahoot.kahoot.service.EstudianteService;
import com.devkahoot.kahoot.service.PartidaService;
import com.devkahoot.kahoot.service.PreguntaService;
import com.devkahoot.kahoot.service.RespuestaDuplicadaException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartidaServiceTest {

	@Test
	void cuandoResponderDosVecesLaMismaPregunta_debeRechazarLaSegundaRespuesta() {
		PreguntaService preguntaService = new PreguntaService();
		EstudianteService estudianteService = new EstudianteService();
		PartidaService partidaService = new PartidaService(preguntaService, estudianteService);

		Pregunta pregunta = new Pregunta();
		pregunta.setEnunciado("¿Qué instrucción consulta datos en SQL?");
		pregunta.setOpciones(List.of("SELECT", "DELETE"));
		pregunta.setRespuestaCorrecta("SELECT");
		preguntaService.agregarNuevaPregunta(pregunta);

		Partida partida = new Partida("ABC123", "Quiz de SQL");
		partida.setIdsPreguntas(List.of(pregunta.getId()));
		partidaService.crearNuevaPartida(partida);

		Estudiante estudiante = new Estudiante("jpro24");
		estudiante.setCodigoPartida("ABC123");
		Estudiante registrado = partidaService.agregarEstudianteAPartida("ABC123", estudiante);

		assertNotNull(registrado);

		partidaService.actualizarEstadoPartida("ABC123", EstadoPartida.EN_CURSO);

		Respuesta respuesta = new Respuesta(registrado.getId(), pregunta.getId(), "SELECT");
		ResultadoRespuesta resultado = partidaService.registrarRespuesta("ABC123", respuesta);

		assertTrue(resultado.isCorrecta());
		assertEquals(100, resultado.getPuntosObtenidos());
		assertEquals(100, registrado.getPuntaje());

		Respuesta respuestaRepetida = new Respuesta(registrado.getId(), pregunta.getId(), "DELETE");
		assertThrows(RespuestaDuplicadaException.class,
				() -> partidaService.registrarRespuesta("ABC123", respuestaRepetida));
		assertEquals(100, registrado.getPuntaje());
	}
}
