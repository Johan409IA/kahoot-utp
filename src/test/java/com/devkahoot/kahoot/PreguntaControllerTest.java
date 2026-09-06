package com.devkahoot.kahoot;

import java.util.List;

import com.devkahoot.kahoot.service.PartidaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.devkahoot.kahoot.controller.PreguntaController;
import com.devkahoot.kahoot.model.Pregunta;
import com.devkahoot.kahoot.service.PreguntaService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PreguntaControllerTest {

	@Test
	void cuandoObtenerTodasLasPreguntas_desdeControlador_debeDelegarEnServicio() {
		PreguntaService preguntaService = mock(PreguntaService.class);
		PreguntaController preguntaController = new PreguntaController(preguntaService, mock(PartidaService.class));
		List<Pregunta> preguntas = List.of(new Pregunta());
		when(preguntaService.obtenerTodasLasPreguntas()).thenReturn(preguntas);

		ResponseEntity<List<Pregunta>> respuesta = preguntaController.obtenerTodasLasPreguntas();

		verify(preguntaService).obtenerTodasLasPreguntas();
		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals(preguntas, respuesta.getBody());
	}
}
