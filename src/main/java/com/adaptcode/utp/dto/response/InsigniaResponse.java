package com.adaptcode.utp.dto.response;

import com.adaptcode.utp.model.TipoInsignia;

public record InsigniaResponse(
		Long id,
		Long usuarioId,
		TipoInsignia tipo) {
}
