package com.adaptcode.utp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptcode.utp.model.AnulacionPregunta;
import com.adaptcode.utp.model.ParticipacionSala;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.RespuestaSala;
import com.adaptcode.utp.model.Sala;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.AnulacionPreguntaRepository;
import com.adaptcode.utp.repository.ParticipacionSalaRepository;
import com.adaptcode.utp.repository.RespuestaSalaRepository;
import com.adaptcode.utp.repository.SalaRepository;

class RankingSalaServiceTest {

	private SalaRepository salaRepository;
	private ParticipacionSalaRepository participacionSalaRepository;
	private RespuestaSalaRepository respuestaSalaRepository;
	private AnulacionPreguntaRepository anulacionPreguntaRepository;
	private RankingSalaServiceImp rankingSalaService;

	@BeforeEach
	void configurar() {
		salaRepository = mock(SalaRepository.class);
		participacionSalaRepository = mock(ParticipacionSalaRepository.class);
		respuestaSalaRepository = mock(RespuestaSalaRepository.class);
		anulacionPreguntaRepository = mock(AnulacionPreguntaRepository.class);
		rankingSalaService = new RankingSalaServiceImp(salaRepository,
				participacionSalaRepository, respuestaSalaRepository,
				anulacionPreguntaRepository);
	}

	@Test
	void ranking_ordenaPorPuntosDesempataPorTiempoConSalto() {
		ParticipacionSala primero = participacion(11L, 21L, "Ana", "Paredes");
		ParticipacionSala empatado = participacion(12L, 22L, "Luis", "Quispe");
		ParticipacionSala tercero = participacion(13L, 23L, "Rosa", "Díaz");
		PreguntaDocente valida = pregunta(50L);
		PreguntaDocente anulada = pregunta(51L);
		when(salaRepository.existsById(5L)).thenReturn(true);
		when(participacionSalaRepository.findAllBySala_Id(5L))
				.thenReturn(List.of(primero, empatado, tercero));
		AnulacionPregunta anulacion = mock(AnulacionPregunta.class);
		when(anulacion.getPregunta()).thenReturn(anulada);
		when(anulacionPreguntaRepository.findAllBySala_Id(5L))
				.thenReturn(List.of(anulacion));
		RespuestaSala r1a = respuesta(primero, valida, true, 5000L);
		RespuestaSala r1b = respuesta(primero, valida, true, 4000L);
		RespuestaSala r2a = respuesta(empatado, valida, true, 5000L);
		RespuestaSala r2b = respuesta(empatado, valida, true, 4000L);
		RespuestaSala r3 = respuesta(tercero, valida, true, 4000L);
		RespuestaSala r4 = respuesta(tercero, anulada, true, 1000L);
		when(respuestaSalaRepository.findAllByParticipacion_Sala_Id(5L))
				.thenReturn(List.of(r1a, r1b, r2a, r2b, r3, r4));

		var ranking = rankingSalaService.mostrarRanking(5L);

		assertEquals(List.of(1, 1, 3),
				ranking.stream().map(r -> r.posicion()).toList());
		assertEquals(21L, ranking.get(0).estudianteId());
		assertEquals("P.", ranking.get(0).apellidoInicial());
		assertEquals(2, ranking.get(0).puntos());
		assertEquals(1, ranking.get(2).puntos());
	}

	private ParticipacionSala participacion(
			Long id, Long estudianteId, String nombres, String apellidos) {
		Usuario estudiante = mock(Usuario.class);
		when(estudiante.getId()).thenReturn(estudianteId);
		when(estudiante.getNombres()).thenReturn(nombres);
		when(estudiante.getApellidos()).thenReturn(apellidos);
		Sala sala = mock(Sala.class);
		when(sala.getId()).thenReturn(5L);
		ParticipacionSala participacion = mock(ParticipacionSala.class);
		when(participacion.getId()).thenReturn(id);
		when(participacion.getSala()).thenReturn(sala);
		when(participacion.getEstudiante()).thenReturn(estudiante);
		return participacion;
	}

	private PreguntaDocente pregunta(Long id) {
		PreguntaDocente pregunta = mock(PreguntaDocente.class);
		when(pregunta.getId()).thenReturn(id);
		return pregunta;
	}

	private RespuestaSala respuesta(ParticipacionSala participacion,
			PreguntaDocente pregunta, boolean acierto, Long tiempoMs) {
		RespuestaSala respuesta = mock(RespuestaSala.class);
		when(respuesta.getParticipacion()).thenReturn(participacion);
		when(respuesta.getPregunta()).thenReturn(pregunta);
		when(respuesta.isAcierto()).thenReturn(acierto);
		when(respuesta.isOmitida()).thenReturn(false);
		when(respuesta.getTiempoMs()).thenReturn(tiempoMs);
		return respuesta;
	}
}
