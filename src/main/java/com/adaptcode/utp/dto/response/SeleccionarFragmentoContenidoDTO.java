package com.adaptcode.utp.dto.response;

import java.util.List;

public record SeleccionarFragmentoContenidoDTO(List<FragmentoMicroRetoResponseDTO> opciones)
		implements ContenidoMicroRetoResponseDTO {
}
