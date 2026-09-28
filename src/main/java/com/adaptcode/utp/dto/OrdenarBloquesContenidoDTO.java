package com.adaptcode.utp.dto;

import java.util.List;

public record OrdenarBloquesContenidoDTO(List<BloqueMicroRetoResponseDTO> bloques)
		implements ContenidoMicroRetoResponseDTO {
}
