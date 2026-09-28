package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.MicroRetoDetalleResponseDTO;
import com.adaptcode.utp.dto.MicroRetoResumenResponseDTO;
import com.adaptcode.utp.model.NivelMicroReto;

public interface MicroRetoService {

	List<MicroRetoResumenResponseDTO> obtenerResumenes(Long cursoId, Long temaId, NivelMicroReto nivel);

	MicroRetoDetalleResponseDTO obtenerDetalle(Long id);
}
