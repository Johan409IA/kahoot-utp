package com.adaptcode.utp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.response.RankingSalaResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.ParticipacionSala;
import com.adaptcode.utp.model.RespuestaSala;
import com.adaptcode.utp.repository.AnulacionPreguntaRepository;
import com.adaptcode.utp.repository.ParticipacionSalaRepository;
import com.adaptcode.utp.repository.RespuestaSalaRepository;
import com.adaptcode.utp.repository.SalaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingSalaServiceImp implements RankingSalaService {

	private final SalaRepository salaRepository;
	private final ParticipacionSalaRepository participacionSalaRepository;
	private final RespuestaSalaRepository respuestaSalaRepository;
	private final AnulacionPreguntaRepository anulacionPreguntaRepository;

	@Override
	@Transactional(readOnly = true)
	public List<RankingSalaResponse> mostrarRanking(Long salaId) {
		if (salaId == null || salaId <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
		if (!salaRepository.existsById(salaId)) {
			throw new ResourceNotFoundException("La sala no existe.");
		}
		List<ParticipacionSala> participaciones = participacionSalaRepository
				.findAllBySala_Id(salaId);
		Set<Long> anuladas = anulacionPreguntaRepository.findAllBySala_Id(salaId)
				.stream()
				.map(a -> a.getPregunta().getId())
				.collect(Collectors.toSet());
		Map<Long, List<RespuestaSala>> porParticipacion =
				respuestaSalaRepository.findAllByParticipacion_Sala_Id(salaId)
						.stream()
						.collect(Collectors.groupingBy(
								r -> r.getParticipacion().getId()));
		return ordenarRanking(calcularPuntajes(
				participaciones, porParticipacion, anuladas));
	}

	private List<Puntaje> calcularPuntajes(
			List<ParticipacionSala> participaciones,
			Map<Long, List<RespuestaSala>> porParticipacion,
			Set<Long> anuladas) {
		List<Puntaje> puntajes = new ArrayList<>();
		for (ParticipacionSala p : participaciones) {
			List<RespuestaSala> respuestas = porParticipacion.getOrDefault(
					p.getId(), List.of()).stream()
					.filter(r -> !anuladas.contains(r.getPregunta().getId()))
					.toList();
			int puntos = (int) respuestas.stream()
					.filter(r -> r.isAcierto() && !r.isOmitida())
					.count();
			long tiempo = respuestas.stream()
					.mapToLong(r -> r.getTiempoMs() != null ? r.getTiempoMs() : 0L)
					.sum();
			puntajes.add(new Puntaje(p, puntos, tiempo));
		}
		puntajes.sort(Comparator.comparingInt(Puntaje::puntos).reversed()
				.thenComparingLong(Puntaje::tiempo));
		return puntajes;
	}

	private List<RankingSalaResponse> ordenarRanking(List<Puntaje> puntajes) {
		List<RankingSalaResponse> ranking = new ArrayList<>();
		int posicion = 0;
		int puntosPrevios = -1;
		long tiempoPrevio = -1L;
		for (int i = 0; i < puntajes.size(); i++) {
			Puntaje actual = puntajes.get(i);
			if (actual.puntos() != puntosPrevios || actual.tiempo() != tiempoPrevio) {
				posicion = i + 1;
				puntosPrevios = actual.puntos();
				tiempoPrevio = actual.tiempo();
			}
			String apellidos = actual.participacion().getEstudiante().getApellidos();
			String inicial = apellidos != null && !apellidos.isEmpty()
					? apellidos.charAt(0) + "." : "";
			ranking.add(new RankingSalaResponse(
					posicion,
					actual.participacion().getId(),
					actual.participacion().getEstudiante().getId(),
					actual.participacion().getEstudiante().getNombres(),
					inicial,
					actual.puntos(),
					actual.tiempo()));
		}
		return ranking;
	}

	private record Puntaje(ParticipacionSala participacion, int puntos, long tiempo) {
	}
}
