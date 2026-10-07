package com.adaptcode.utp.dto.response;

import java.util.List;

public record SeleccionMultipleContenidoDTO(List<OpcionMicroRetoResponseDTO> opciones)
		implements ContenidoMicroRetoResponseDTO {
}
