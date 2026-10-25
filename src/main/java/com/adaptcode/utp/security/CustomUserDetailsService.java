package com.adaptcode.utp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Carga usuarios por correo (el username del sistema) y los expone como
 * {@link UsuarioPrincipal}. Las cuentas inactivas quedan deshabilitadas.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
				.orElseThrow(() -> new UsernameNotFoundException(
						"Credenciales inválidas."));
		return new UsuarioPrincipal(
				usuario.getId(),
				usuario.getCorreo(),
				usuario.getContrasenaHash(),
				usuario.getNombres(),
				usuario.getApellidos(),
				usuario.getRol(),
				usuario.getGenero(),
				usuario.getFechaNacimiento(),
				usuario.isActivo(),
				usuario.getCredencialCambiadaEn());
	}
}
