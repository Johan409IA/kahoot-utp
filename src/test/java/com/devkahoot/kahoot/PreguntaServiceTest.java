package com.devkahoot.kahoot;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.devkahoot.kahoot.model.Pregunta;
import com.devkahoot.kahoot.service.PreguntaService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreguntaServiceTest {

	private final PreguntaService preguntaService = new PreguntaService();

	private Pregunta crearPreguntaDeEjemplo(String enunciado) {
		Pregunta pregunta = new Pregunta();
		pregunta.setEnunciado(enunciado);
		pregunta.setOpciones(List.of("Sí", "No"));
		pregunta.setRespuestaCorrecta("Sí");
		pregunta.setExplicacion("Explicación de prueba");
		pregunta.setCategoria("Programación");
		return pregunta;
	}

	@Test
	void cuandoListarPreguntas_debeRetornarTodas() {
		preguntaService.agregarNuevaPregunta(crearPreguntaDeEjemplo("¿Qué es una clase?"));
		preguntaService.agregarNuevaPregunta(crearPreguntaDeEjemplo("¿Qué es un objeto?"));

		List<Pregunta> preguntas = preguntaService.obtenerTodasLasPreguntas();

		assertEquals(2, preguntas.size());
	}

	@Test
	void cuandoAgregarPregunta_debeGuardarla() {
		Pregunta pregunta = crearPreguntaDeEjemplo("¿Qué es una variable?");

		Pregunta guardada = preguntaService.agregarNuevaPregunta(pregunta);

		assertEquals(pregunta, guardada);
		assertTrue(preguntaService.obtenerTodasLasPreguntas().contains(pregunta));
		assertEquals(1, guardada.getId());
		assertEquals(1, guardada.getOrden());
	}

	@Test
	void cuandoActualizarPreguntaExistente_debeModificarla() {
		Pregunta pregunta = preguntaService.agregarNuevaPregunta(crearPreguntaDeEjemplo("¿Qué es una clase?"));
		Pregunta nuevaPregunta = crearPreguntaDeEjemplo("¿Qué es una clase en POO?");
		nuevaPregunta.setRespuestaCorrecta("No");

		Pregunta actualizada = preguntaService.actualizarPregunta(pregunta.getId(), nuevaPregunta);

		assertEquals(pregunta.getId(), actualizada.getId());
		assertEquals("¿Qué es una clase en POO?", actualizada.getEnunciado());
		assertEquals("No", actualizada.getRespuestaCorrecta());
	}

	@Test
	void cuandoEliminarPreguntaExistente_debeQuitarla() {
		Pregunta pregunta = preguntaService.agregarNuevaPregunta(crearPreguntaDeEjemplo("¿Qué es una clase?"));

		boolean eliminada = preguntaService.eliminarPregunta(pregunta.getId());

		assertTrue(eliminada);
		assertFalse(preguntaService.obtenerTodasLasPreguntas().contains(pregunta));
	}
}
