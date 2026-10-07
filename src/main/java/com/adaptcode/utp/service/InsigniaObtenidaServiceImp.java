package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.response.InsigniaResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.InsigniaObtenida;
import com.adaptcode.utp.repository.InsigniaObtenidaRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsigniaObtenidaServiceImp implements InsigniaObtenidaService {

	private final InsigniaObtenidaRepository insigniaObtenidaRepository;
	private final UsuarioRepository usuarioRepository;

	@Override
	@Transactional(readOnly = true)
	public List<InsigniaResponse> listarPorUsuario(Long usuarioId) {
		validarUsuario(usuarioId);
		return insigniaObtenidaRepository.findAllByUsuario_Id(usuarioId).stream()
				.map(this::aResponse)
				.toList();
	}

	private void validarUsuario(Long usuarioId) {
		if (usuarioId == null || usuarioId <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
		if (!usuarioRepository.existsById(usuarioId)) {
			throw new ResourceNotFoundException("El usuario no existe.");
		}
	}

	private InsigniaResponse aResponse(InsigniaObtenida insignia) {
		return new InsigniaResponse(
				insignia.getId(),
				insignia.getUsuario().getId(),
				insignia.getTipo());
	}
}
