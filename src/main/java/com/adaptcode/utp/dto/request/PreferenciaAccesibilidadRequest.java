package com.adaptcode.utp.dto.request;

import jakarta.validation.constraints.Size;

public record PreferenciaAccesibilidadRequest(
		@Size(max = 32, message = "El tema no debe superar los 32 caracteres.") String tema,
		@Size(max = 32, message = "El tamaño no debe superar los 32 caracteres.") String tamanoTexto,
		boolean reduccionMovimiento,
		boolean sonidoActivado,
		boolean modoDaltonismo) {
}
