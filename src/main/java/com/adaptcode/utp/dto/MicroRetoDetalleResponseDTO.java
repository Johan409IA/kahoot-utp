package com.adaptcode.utp.dto;

import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.TipoMicroReto;

public record MicroRetoDetalleResponseDTO(
		Long id,
		Long temaId,
		TipoMicroReto tipo,
		NivelMicroReto nivel,
		String enunciado,
		ContenidoMicroRetoResponseDTO contenido) {
}
