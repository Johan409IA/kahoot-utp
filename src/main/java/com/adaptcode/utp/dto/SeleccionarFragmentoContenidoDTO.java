package com.adaptcode.utp.dto;

import java.util.List;

public record SeleccionarFragmentoContenidoDTO(List<FragmentoMicroRetoResponseDTO> opciones)
		implements ContenidoMicroRetoResponseDTO {
}
