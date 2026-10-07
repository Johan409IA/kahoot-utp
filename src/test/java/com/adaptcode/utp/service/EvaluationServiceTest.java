package com.adaptcode.utp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.adaptcode.utp.dto.request.EvaluarIntentoRequest;
import com.adaptcode.utp.dto.request.IntentoRequest;
import com.adaptcode.utp.dto.response.IntentoResponse;
import com.adaptcode.utp.model.Alternativa;
import com.adaptcode.utp.model.Curso;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.Retroalimentacion;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.model.TipoMicroReto;
import com.adaptcode.utp.repository.MicroRetoRepository;

class EvaluationServiceTest {

	private MicroRetoRepository microRetoRepository;
	private IntentoService intentoService;
	private EvaluationServiceImp evaluationService;

	@BeforeEach
	void configurar() {
		microRetoRepository = mock(MicroRetoRepository.class);
		intentoService = mock(IntentoService.class);
		evaluationService = new EvaluationServiceImp(
				microRetoRepository, intentoService);
	}

	@Test
	void evaluarRespuestaIncorrecta_devuelveFeedbackSinRevelarCorrecta() {
		MicroReto reto = retoSeleccionMultiple();
		when(microRetoRepository
				.findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(
						30L)).thenReturn(Optional.of(reto));
		when(intentoService.registrarIntento(eq(9L), any(IntentoRequest.class)))
				.thenReturn(new IntentoResponse(1L, 9L, 30L, 1, false, false,
						1000L));

		var response = evaluationService.evaluarIntento(9L,
				new EvaluarIntentoRequest(30L, 100L, null, false, 1000L));

		assertFalse(response.acierto());
		assertEquals("Confunde el operador.", response.retroalimentacion().causa());
		assertNull(response.solucion());
		assertFalse(response.verSolucionDisponible());
		verify(intentoService).registrarIntento(eq(9L),
				any(IntentoRequest.class));
	}

	private MicroReto retoSeleccionMultiple() {
		Curso curso = new Curso("Algoritmos");
		ReflectionTestUtils.setField(curso, "id", 1L);
		Tema tema = new Tema(curso, "Condicionales");
		ReflectionTestUtils.setField(tema, "id", 10L);
		MicroReto reto = new MicroReto("DEMO-ALG-01", tema,
				TipoMicroReto.SELECCION_MULTIPLE, NivelMicroReto.BASICO,
				"Evalúa una condición.", null, null);
		ReflectionTestUtils.setField(reto, "id", 30L);
		Alternativa incorrecta = new Alternativa("Falso", null, null, 1, false);
		ReflectionTestUtils.setField(incorrecta, "id", 100L);
		Alternativa correcta = new Alternativa("Verdadero", null, null, 2, true);
		ReflectionTestUtils.setField(correcta, "id", 101L);
		reto.agregarAlternativa(incorrecta);
		reto.agregarAlternativa(correcta);
		reto.agregarRetroalimentacion(new Retroalimentacion(incorrecta,
				"Confunde el operador.", "La condición usa >=.", "Paso 1.",
				"Reintenta."));
		return reto;
	}
}
