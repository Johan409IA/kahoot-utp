package com.adaptcode.utp.dto.response;

public record RankingSalaResponse(
		int posicion,
		Long participacionId,
		Long estudianteId,
		String nombre,
		String apellidoInicial,
		int puntos,
		long tiempoMs) {
}
