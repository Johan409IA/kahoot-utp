package com.adaptcode.utp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.adaptcode.utp.model.Curso;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.repository.CursoRepository;
import com.adaptcode.utp.repository.TemaRepository;

class TemaServiceTest {

	private CursoRepository cursoRepository;
	private TemaRepository temaRepository;
	private TemaServiceImp temaService;

	@BeforeEach
	void configurar() {
		cursoRepository = mock(CursoRepository.class);
		temaRepository = mock(TemaRepository.class);
		temaService = new TemaServiceImp(cursoRepository, temaRepository);
	}

	@Test
	void obtenerTemasActivosPorCurso_consultaSoloTemasActivosYConservaElOrdenDelRepositorio() {
		Curso curso = new Curso("Algoritmos");
		when(cursoRepository.existsByIdAndActivoTrue(1L)).thenReturn(true);
		when(temaRepository.findAllByCurso_IdAndActivoTrueOrderByNombreAsc(1L)).thenReturn(List.of(
				new Tema(curso, "Condicionales"), new Tema(curso, "Secuencias")));

		var temas = temaService.obtenerTemasActivosPorCurso(1L);

		assertEquals(List.of("Condicionales", "Secuencias"), temas.stream().map(t -> t.nombre()).toList());
		verify(temaRepository).findAllByCurso_IdAndActivoTrueOrderByNombreAsc(1L);
	}

	@Test
	void obtenerTemasActivosPorCurso_devuelve404SiElCursoNoExiste() {
		when(cursoRepository.existsByIdAndActivoTrue(99L)).thenReturn(false);

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> temaService.obtenerTemasActivosPorCurso(99L));

		assertEquals(404, error.getStatusCode().value());
	}

	@Test
	void obtenerTemasActivosPorCurso_devuelve400SiElIdentificadorNoEsPositivo() {
		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> temaService.obtenerTemasActivosPorCurso(0L));

		assertEquals(400, error.getStatusCode().value());
	}
}
