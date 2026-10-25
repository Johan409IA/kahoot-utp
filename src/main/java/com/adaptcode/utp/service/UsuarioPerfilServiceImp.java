package com.adaptcode.utp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.UsuarioPerfilRequest;
import com.adaptcode.utp.dto.response.UsuarioPerfilResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Completa los datos demográficos del usuario (género y fecha de nacimiento)
 * cuando faltan, p. ej. en cuentas institucionales creadas antes de que el
 * registro los pidiera.
 */
@Service
@RequiredArgsConstructor
public class UsuarioPerfilServiceImp implements UsuarioPerfilService {

	private final UsuarioRepository usuarioRepository;

	@Override
	@Transactional
	public UsuarioPerfilResponse completar(Long usuarioId, UsuarioPerfilRequest request) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El usuario no existe."));
		usuario.setGenero(request.genero());
		usuario.setFechaNacimiento(request.fechaNacimiento());
		usuarioRepository.save(usuario);
		return new UsuarioPerfilResponse(usuario.getId(), usuario.getGenero(),
				usuario.getFechaNacimiento());
	}
}
