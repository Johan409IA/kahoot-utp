package com.adaptcode.utp.dto.response;

import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.OrigenPregunta;
import com.adaptcode.utp.model.TipoMicroReto;

public record PreguntaDocenteResponse(
		Long id,
		Long docenteId,
		OrigenPregunta origen,
		Long microRetoOrigenId,
		Long temaId,
		TipoMicroReto tipo,
		NivelMicroReto nivel,
		String enunciado,
		boolean activa) {
}
