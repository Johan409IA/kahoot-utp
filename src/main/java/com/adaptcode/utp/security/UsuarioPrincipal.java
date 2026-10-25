package com.adaptcode.utp.security;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.adaptcode.utp.model.GeneroUsuario;
import com.adaptcode.utp.model.RolUsuario;

import lombok.Getter;

/**
 * Principal autenticado. Expone el identificador y el rol para que los
 * controladores resuelvan al usuario desde el token JWT en lugar del path.
 */
@Getter
public class UsuarioPrincipal implements UserDetails {

	private final Long id;
	private final String correo;
	private final String contrasenaHash;
	private final String nombres;
	private final String apellidos;
	private final RolUsuario rol;
	private final GeneroUsuario genero;
	private final LocalDate fechaNacimiento;
	private final boolean activo;
	private final Instant credencialCambiadaEn;

	public UsuarioPrincipal(Long id, String correo, String contrasenaHash, String nombres,
			String apellidos, RolUsuario rol, GeneroUsuario genero,
			LocalDate fechaNacimiento, boolean activo, Instant credencialCambiadaEn) {
		this.id = id;
		this.correo = correo;
		this.contrasenaHash = contrasenaHash;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.rol = rol;
		this.genero = genero;
		this.fechaNacimiento = fechaNacimiento;
		this.activo = activo;
		this.credencialCambiadaEn = credencialCambiadaEn;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
	}

	@Override
	public String getPassword() {
		return contrasenaHash;
	}

	@Override
	public String getUsername() {
		return correo;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return activo;
	}
}
