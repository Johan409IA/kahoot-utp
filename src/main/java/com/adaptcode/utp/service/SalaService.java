package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.SalaRequest;
import com.adaptcode.utp.dto.response.SalaResponse;
import com.adaptcode.utp.model.EstadoSala;

public interface SalaService {

	SalaResponse crearSala(Long docenteId, SalaRequest request);

	SalaResponse mostrarPorId(Long docenteId, Long id);

	SalaResponse mostrarPorPin(String pin);

	List<SalaResponse> listarPorDocente(Long docenteId, EstadoSala estado);

	SalaResponse abrirEspera(Long docenteId, Long id);

	SalaResponse iniciarSala(Long docenteId, Long id);

	SalaResponse pausarSala(Long docenteId, Long id);

	SalaResponse reanudarSala(Long docenteId, Long id);

	SalaResponse finalizarSala(Long docenteId, Long id);
}
