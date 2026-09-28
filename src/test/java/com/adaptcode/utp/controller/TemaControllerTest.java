package com.adaptcode.utp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.adaptcode.utp.dto.TemaResponseDTO;
import com.adaptcode.utp.service.TemaService;

@WebMvcTest(TemaController.class)
class TemaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TemaService temaService;

	@Test
	void obtenerTemas_devuelveLosTemasActivosDelCurso() throws Exception {
		when(temaService.obtenerTemasActivosPorCurso(1L)).thenReturn(List.of(
				new TemaResponseDTO(10L, "Condicionales"),
				new TemaResponseDTO(11L, "Secuencias")));

		mockMvc.perform(get("/api/cursos/1/temas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(10))
				.andExpect(jsonPath("$[0].nombre").value("Condicionales"))
				.andExpect(jsonPath("$[1].nombre").value("Secuencias"));
	}

	@Test
	void obtenerTemas_devuelveListaVaciaCuandoElCursoNoTieneTemasActivos() throws Exception {
		when(temaService.obtenerTemasActivosPorCurso(1L)).thenReturn(List.of());

		mockMvc.perform(get("/api/cursos/1/temas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void obtenerTemas_devuelve404CuandoElCursoNoExiste() throws Exception {
		when(temaService.obtenerTemasActivosPorCurso(999L))
				.thenThrow(new org.springframework.web.server.ResponseStatusException(
						org.springframework.http.HttpStatus.NOT_FOUND));

		mockMvc.perform(get("/api/cursos/999/temas"))
				.andExpect(status().isNotFound());
	}
}
