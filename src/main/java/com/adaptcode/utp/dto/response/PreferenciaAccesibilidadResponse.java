package com.adaptcode.utp.dto.response;

public record PreferenciaAccesibilidadResponse(
		Long id,
		Long usuarioId,
		String tema,
		String tamanoTexto,
		boolean reduccionMovimiento,
		boolean sonidoActivado,
		boolean modoDaltonismo) {
}
