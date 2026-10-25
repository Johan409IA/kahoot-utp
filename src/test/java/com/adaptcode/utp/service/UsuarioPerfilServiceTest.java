package com.adaptcode.utp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptcode.utp.dto.request.UsuarioPerfilRequest;
import com.adaptcode.utp.dto.response.UsuarioPerfilResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.model.GeneroUsuario;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.UsuarioRepository;

class UsuarioPerfilServiceTest {

	private UsuarioRepository usuarioRepository;
	private UsuarioPerfilServiceImp usuarioPerfilService;

	@BeforeEach
	void configurar() {
		usuarioRepository = mock(UsuarioRepository.class);
		usuarioPerfilService = new UsuarioPerfilServiceImp(usuarioRepository);
	}

	@Test
	void completaGeneroYFechaDeNacimiento() {
		Usuario usuario = new Usuario("u12345678@utp.edu.pe", "Ana", "Torres",
				"hash", RolUsuario.ESTUDIANTE_UTP, true, null, null);
		usuario.setId(7L);
		when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

		UsuarioPerfilResponse response = usuarioPerfilService.completar(7L,
				new UsuarioPerfilRequest(GeneroUsuario.FEMENINO,
						LocalDate.of(2003, 5, 14)));

		assertEquals(7L, response.usuarioId());
		assertEquals(GeneroUsuario.FEMENINO, response.genero());
		assertEquals(LocalDate.of(2003, 5, 14), response.fechaNacimiento());
		assertEquals(GeneroUsuario.FEMENINO, usuario.getGenero());
		assertEquals(LocalDate.of(2003, 5, 14), usuario.getFechaNacimiento());
		verify(usuarioRepository).save(usuario);
	}

	@Test
	void lanzaErrorCuandoElUsuarioNoExiste() {
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> usuarioPerfilService
				.completar(99L, new UsuarioPerfilRequest(GeneroUsuario.MASCULINO,
						LocalDate.of(2000, 1, 1))));
	}
}
