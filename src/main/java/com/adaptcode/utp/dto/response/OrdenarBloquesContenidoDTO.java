package com.adaptcode.utp.dto.response;

import java.util.List;

public record OrdenarBloquesContenidoDTO(List<BloqueMicroRetoResponseDTO> bloques)
		implements ContenidoMicroRetoResponseDTO {
}
