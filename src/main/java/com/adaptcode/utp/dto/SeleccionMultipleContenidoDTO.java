package com.adaptcode.utp.dto;

import java.util.List;

public record SeleccionMultipleContenidoDTO(List<OpcionMicroRetoResponseDTO> opciones)
		implements ContenidoMicroRetoResponseDTO {
}
