package com.adaptcode.utp.dto.response;

import java.time.Instant;

import com.adaptcode.utp.model.AmpliacionSala;
import com.adaptcode.utp.model.EstadoSala;

public record SalaResponse(
		Long id,
		Long docenteId,
		Long quizId,
		String pin,
		EstadoSala estado,
		Integer tiempoPorPregunta,
		AmpliacionSala ampliacion,
		Instant quizVersionCongeladaEn) {
}
