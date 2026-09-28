package com.adaptcode.utp.dto;

import java.util.List;

public record PredecirSalidaContenidoDTO(
		CodigoMicroRetoResponseDTO codigo,
		List<OpcionMicroRetoResponseDTO> opciones)
		implements ContenidoMicroRetoResponseDTO {
}
