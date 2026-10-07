package com.adaptcode.utp.dto.response;

public record ParticipacionSalaResponse(
		Long id,
		Long salaId,
		Long estudianteId,
		boolean expulsado) {
}
