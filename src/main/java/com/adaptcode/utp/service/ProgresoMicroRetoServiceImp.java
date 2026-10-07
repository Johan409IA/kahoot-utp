package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.response.ProgresoMicroRetoResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.ProgresoMicroReto;
import com.adaptcode.utp.repository.ProgresoMicroRetoRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgresoMicroRetoServiceImp implements ProgresoMicroRetoService {

	private final ProgresoMicroRetoRepository progresoMicroRetoRepository;
	private final UsuarioRepository usuarioRepository;

	@Override
	@Transactional(readOnly = true)
	public ProgresoMicroRetoResponse mostrarPorUsuarioYReto(Long usuarioId, Long microRetoId) {
		validarUsuario(usuarioId);
		validarIdentificador(microRetoId);
		return aResponse(progresoMicroRetoRepository
				.findByUsuario_IdAndMicroReto_Id(usuarioId, microRetoId)
				.orElseThrow(() -> new ResourceNotFoundException("El progreso no existe.")));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProgresoMicroRetoResponse> listarPorUsuario(Long usuarioId) {
		validarUsuario(usuarioId);
		return progresoMicroRetoRepository.findAllByUsuario_Id(usuarioId).stream()
				.map(this::aResponse)
				.toList();
	}

	private void validarUsuario(Long usuarioId) {
		validarIdentificador(usuarioId);
		if (!usuarioRepository.existsById(usuarioId)) {
			throw new ResourceNotFoundException("El usuario no existe.");
		}
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private ProgresoMicroRetoResponse aResponse(ProgresoMicroReto progreso) {
		return new ProgresoMicroRetoResponse(
				progreso.getId(),
				progreso.getUsuario().getId(),
				progreso.getMicroReto().getId(),
				progreso.isCompletado(),
				progreso.isResueltoConAyuda(),
				progreso.isPuntoDominio());
	}
}
