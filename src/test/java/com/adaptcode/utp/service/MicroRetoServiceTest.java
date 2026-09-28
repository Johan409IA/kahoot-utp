package com.adaptcode.utp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.adaptcode.utp.model.Alternativa;
import com.adaptcode.utp.model.Curso;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.model.TipoMicroReto;
import com.adaptcode.utp.repository.CursoRepository;
import com.adaptcode.utp.repository.MicroRetoRepository;
import com.adaptcode.utp.repository.TemaRepository;

class MicroRetoServiceTest {

	private CursoRepository cursoRepository;
	private TemaRepository temaRepository;
	private MicroRetoRepository microRetoRepository;
	private MicroRetoServiceImp microRetoService;

	@BeforeEach
	void configurar() {
		cursoRepository = mock(CursoRepository.class);
		temaRepository = mock(TemaRepository.class);
		microRetoRepository = mock(MicroRetoRepository.class);
		microRetoService = new MicroRetoServiceImp(cursoRepository, temaRepository, microRetoRepository);
	}

	@Test
	void obtenerResumenes_filtraPorCursoTemaYNivel() {
		Curso curso = new Curso("Algoritmos");
		ReflectionTestUtils.setField(curso, "id", 1L);
		Tema tema = new Tema(curso, "Condicionales");
		ReflectionTestUtils.setField(tema, "id", 10L);
		when(cursoRepository.existsByIdAndActivoTrue(1L)).thenReturn(true);
		when(temaRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(tema));
		MicroReto reto = new MicroReto("DEMO-ALG-01", tema,
				TipoMicroReto.SELECCION_MULTIPLE, NivelMicroReto.BASICO, "Evalúa una condición.", null, null);
		reto.agregarAlternativa(new Alternativa("Verdadero", null, null, 1, true));
		when(microRetoRepository.findAllByTema_Curso_IdAndTema_IdAndTema_ActivoTrueAndActivoTrueAndNivelOrderByEnunciadoAsc(
				1L, 10L, NivelMicroReto.BASICO)).thenReturn(List.of(reto));

		var resumenes = microRetoService.obtenerResumenes(1L, 10L, NivelMicroReto.BASICO);

		assertEquals(1, resumenes.size());
		assertEquals("Evalúa una condición.", resumenes.getFirst().enunciado());
		verify(microRetoRepository)
				.findAllByTema_Curso_IdAndTema_IdAndTema_ActivoTrueAndActivoTrueAndNivelOrderByEnunciadoAsc(
						1L, 10L, NivelMicroReto.BASICO);
	}

	@Test
	void obtenerResumenes_rechazaUnTemaQuePerteneceAOtroCurso() {
		Curso curso = new Curso("Python");
		ReflectionTestUtils.setField(curso, "id", 2L);
		when(cursoRepository.existsByIdAndActivoTrue(1L)).thenReturn(true);
		Tema tema = new Tema(curso, "Expresiones");
		ReflectionTestUtils.setField(tema, "id", 10L);
		when(temaRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(tema));

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> microRetoService.obtenerResumenes(1L, 10L, null));

		assertEquals(400, error.getStatusCode().value());
	}

	@Test
	void obtenerDetalle_mapeaOpcionesSinAgregarCamposDeRespuestaCorrecta() {
		Curso curso = new Curso("Algoritmos");
		ReflectionTestUtils.setField(curso, "id", 1L);
		Tema tema = new Tema(curso, "Condicionales");
		ReflectionTestUtils.setField(tema, "id", 10L);
		MicroReto reto = new MicroReto("DEMO-ALG-01", tema,
				TipoMicroReto.SELECCION_MULTIPLE, NivelMicroReto.BASICO, "Evalúa una condición.", null, null);
		reto.agregarAlternativa(new Alternativa("Verdadero", null, null, 1, true));
		when(microRetoRepository.findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(1L))
				.thenReturn(Optional.of(reto));

		var detalle = microRetoService.obtenerDetalle(1L);

		assertEquals(TipoMicroReto.SELECCION_MULTIPLE, detalle.tipo());
		assertEquals("Verdadero", ((com.adaptcode.utp.dto.SeleccionMultipleContenidoDTO) detalle.contenido())
				.opciones().getFirst().texto());
	}
}
