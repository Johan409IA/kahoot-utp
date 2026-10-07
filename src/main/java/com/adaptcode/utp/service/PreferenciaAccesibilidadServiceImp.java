package com.adaptcode.utp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.PreferenciaAccesibilidadRequest;
import com.adaptcode.utp.dto.response.PreferenciaAccesibilidadResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.PreferenciaAccesibilidad;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.PreferenciaAccesibilidadRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferenciaAccesibilidadServiceImp
		implements PreferenciaAccesibilidadService {

	private final PreferenciaAccesibilidadRepository preferenciaAccesibilidadRepository;
	private final UsuarioRepository usuarioRepository;

	@Override
	@Transactional(readOnly = true)
	public PreferenciaAccesibilidadResponse mostrarPorUsuario(Long usuarioId) {
		validarUsuarioExiste(usuarioId);
		PreferenciaAccesibilidad preferencia = preferenciaAccesibilidadRepository
				.findByUsuario_Id(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El usuario no tiene preferencias registradas."));
		return aResponse(preferencia);
	}

	@Override
	@Transactional
	public PreferenciaAccesibilidadResponse guardarPreferencias(
			Long usuarioId, PreferenciaAccesibilidadRequest request) {
		Usuario usuario = validarUsuarioExiste(usuarioId);
		PreferenciaAccesibilidad preferencia = preferenciaAccesibilidadRepository
				.findByUsuario_Id(usuarioId)
				.orElseGet(() -> new PreferenciaAccesibilidad(usuario));
		if (request.tema() != null) {
			preferencia.setTema(request.tema());
		}
		if (request.tamanoTexto() != null) {
			preferencia.setTamanoTexto(request.tamanoTexto());
		}
		preferencia.setReduccionMovimiento(request.reduccionMovimiento());
		preferencia.setSonidoActivado(request.sonidoActivado());
		preferencia.setModoDaltonismo(request.modoDaltonismo());
		return aResponse(preferenciaAccesibilidadRepository.save(preferencia));
	}

	private Usuario validarUsuarioExiste(Long usuarioId) {
		if (usuarioId == null || usuarioId <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
		return usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El usuario no existe."));
	}

	private PreferenciaAccesibilidadResponse aResponse(
			PreferenciaAccesibilidad preferencia) {
		return new PreferenciaAccesibilidadResponse(
				preferencia.getId(),
				preferencia.getUsuario().getId(),
				preferencia.getTema(),
				preferencia.getTamanoTexto(),
				preferencia.isReduccionMovimiento(),
				preferencia.isSonidoActivado(),
				preferencia.isModoDaltonismo());
	}
}
