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

import com.adaptcode.utp.dto.CursoResponseDTO;
import com.adaptcode.utp.service.CursoService;

@WebMvcTest(CursoController.class)
class CursoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CursoService cursoService;

	@Test
	void obtenerCursos_devuelveLosCursosInstitucionalesComoDto() throws Exception {
		when(cursoService.obtenerCursosActivos()).thenReturn(List.of(
				new CursoResponseDTO(1L, "Algoritmos"),
				new CursoResponseDTO(2L, "Bases de Datos"),
				new CursoResponseDTO(3L, "Python")));

		mockMvc.perform(get("/api/cursos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].nombre").value("Algoritmos"))
				.andExpect(jsonPath("$[1].nombre").value("Bases de Datos"))
				.andExpect(jsonPath("$[2].nombre").value("Python"))
				.andExpect(jsonPath("$[0].activo").doesNotExist());
	}

	@Test
	void obtenerCursos_devuelveListaVaciaCuandoNoHayCursosActivos() throws Exception {
		when(cursoService.obtenerCursosActivos()).thenReturn(List.of());

		mockMvc.perform(get("/api/cursos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}
}
