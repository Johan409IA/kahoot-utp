package com.adaptcode.utp.dto.response;

import java.time.Instant;

/**
 * Respuesta con el código generado. En producción el código se envía por
 * correo; para la etapa local sin servicio de correo se devuelve para pruebas.
 */
public record CodigoResponse(
		String mensaje,
		String codigo,
		Instant expiraEn) {
}
