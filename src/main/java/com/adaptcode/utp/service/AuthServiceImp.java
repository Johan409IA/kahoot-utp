package com.adaptcode.utp.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.LoginRequest;
import com.adaptcode.utp.dto.request.RecuperarContrasenaRequest;
import com.adaptcode.utp.dto.request.ReenviarCodigoRequest;
import com.adaptcode.utp.dto.request.RegistroRequest;
import com.adaptcode.utp.dto.request.RestablecerContrasenaRequest;
import com.adaptcode.utp.dto.request.VerificarCodigoRequest;
import com.adaptcode.utp.dto.response.AuthResponse;
import com.adaptcode.utp.dto.response.CodigoResponse;
import com.adaptcode.utp.dto.response.MensajeResponse;
import com.adaptcode.utp.dto.response.RegistroResponse;
import com.adaptcode.utp.exception.AutenticacionException;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.CodigoVerificacion;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.TipoCodigoVerificacion;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.CodigoVerificacionRepository;
import com.adaptcode.utp.repository.UsuarioRepository;
import com.adaptcode.utp.security.JwtService;
import com.adaptcode.utp.security.UsuarioPrincipal;

import lombok.RequiredArgsConstructor;

/**
 * Registro, inicio de sesión y verificación de correo. El rol se deriva del
 * patrón del correo institucional; los correos externos requieren código.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

	private static final Pattern CORREO_ESTUDIANTE = Pattern
			.compile("^u\\d{8}@utp\\.edu\\.pe$");
	private static final Pattern CORREO_DOCENTE = Pattern
			.compile("^c\\d{5}@utp\\.edu\\.pe$");
	private static final String DOMINIO_UTP = "@utp.edu.pe";
	private static final String MENSAJE_NEUTRAL = "Si el correo está registrado, "
			+ "recibirás un código.";

	private final UsuarioRepository usuarioRepository;
	private final CodigoVerificacionRepository codigoVerificacionRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final SecureRandom random = new SecureRandom();

	@Value("${seguridad.codigo-expiracion-minutos:10}")
	private long minutosExpiracionCodigo;

	@Override
	@Transactional
	public RegistroResponse registrar(RegistroRequest request) {
		String correo = normalizarCorreo(request.correo());
		if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
			throw new SolicitudInvalidaException("El correo ya está registrado.");
		}
		RolUsuario rol = resolverRol(correo);
		String hash = passwordEncoder.encode(request.contrasena());
		Usuario usuario = usuarioRepository.save(new Usuario(correo,
				request.nombres().trim(), request.apellidos().trim(), hash, rol,
				false, request.genero(), request.fechaNacimiento()));
		CodigoVerificacion codigo = generarCodigo(usuario,
				TipoCodigoVerificacion.VERIFICACION_CUENTA);
		return new RegistroResponse(usuario.getId(), correo, rol, true,
				codigo.getCodigo(), codigo.getExpiraEn());
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String correo = normalizarCorreo(request.correo());
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(correo,
							request.contrasena()));
			UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
			String token = jwtService.generateToken(principal);
			return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(),
					principal.getId(), principal.getCorreo(), principal.getNombres(),
					principal.getApellidos(), principal.getRol(), principal.getGenero(),
					principal.getFechaNacimiento());
		} catch (DisabledException ex) {
			throw new AutenticacionException("La cuenta no está verificada. "
					+ "Confirme el código enviado a su correo.");
		} catch (BadCredentialsException ex) {
			throw new AutenticacionException("Correo o contraseña incorrectos.");
		} catch (AuthenticationException ex) {
			throw new AutenticacionException("No se pudo iniciar sesión.");
		}
	}

	@Override
	@Transactional
	public MensajeResponse verificarCuenta(VerificarCodigoRequest request) {
		String correo = normalizarCorreo(request.correo());
		Usuario usuario = buscarUsuario(correo);
		if (usuario.isActivo()) {
			return new MensajeResponse("La cuenta ya está verificada.");
		}
		CodigoVerificacion codigo = buscarCodigoValido(correo,
				TipoCodigoVerificacion.VERIFICACION_CUENTA, request.codigo());
		codigo.setUsado(true);
		codigoVerificacionRepository.save(codigo);
		usuario.setActivo(true);
		usuarioRepository.save(usuario);
		return new MensajeResponse("Cuenta verificada. Ya puede iniciar sesión.");
	}

	@Override
	@Transactional
	public CodigoResponse reenviarCodigoVerificacion(ReenviarCodigoRequest request) {
		String correo = normalizarCorreo(request.correo());
		Optional<Usuario> usuario = usuarioRepository.findByCorreoIgnoreCase(correo);
		if (usuario.isEmpty() || usuario.get().isActivo()) {
			return new CodigoResponse(MENSAJE_NEUTRAL, null, null);
		}
		CodigoVerificacion codigo = generarCodigo(usuario.get(),
				TipoCodigoVerificacion.VERIFICACION_CUENTA);
		return new CodigoResponse(MENSAJE_NEUTRAL, codigo.getCodigo(),
				codigo.getExpiraEn());
	}

	@Override
	@Transactional
	public CodigoResponse solicitarRecuperacion(RecuperarContrasenaRequest request) {
		String correo = normalizarCorreo(request.correo());
		Optional<Usuario> usuario = usuarioRepository.findByCorreoIgnoreCase(correo);
		if (usuario.isEmpty() || !usuario.get().isActivo()) {
			return new CodigoResponse(MENSAJE_NEUTRAL, null, null);
		}
		CodigoVerificacion codigo = generarCodigo(usuario.get(),
				TipoCodigoVerificacion.RECUPERACION);
		return new CodigoResponse(MENSAJE_NEUTRAL, codigo.getCodigo(),
				codigo.getExpiraEn());
	}

	@Override
	@Transactional
	public MensajeResponse restablecerContrasena(RestablecerContrasenaRequest request) {
		String correo = normalizarCorreo(request.correo());
		Usuario usuario = buscarUsuario(correo);
		CodigoVerificacion codigo = buscarCodigoValido(correo,
				TipoCodigoVerificacion.RECUPERACION, request.codigo());
		codigo.setUsado(true);
		codigoVerificacionRepository.save(codigo);
		usuario.setContrasenaHash(passwordEncoder.encode(request.nuevaContrasena()));
		usuario.setCredencialCambiadaEn(Instant.now());
		usuarioRepository.save(usuario);
		return new MensajeResponse("Contraseña actualizada. Inicie sesión nuevamente.");
	}

	private RolUsuario resolverRol(String correo) {
		if (CORREO_ESTUDIANTE.matcher(correo).matches()) {
			return RolUsuario.ESTUDIANTE_UTP;
		}
		if (CORREO_DOCENTE.matcher(correo).matches()) {
			return RolUsuario.DOCENTE_UTP;
		}
		if (correo.endsWith(DOMINIO_UTP)) {
			throw new SolicitudInvalidaException("El formato del correo institucional "
					+ "no es válido. Use U+8 dígitos o C+5 dígitos @utp.edu.pe.");
		}
		return RolUsuario.USUARIO_EXTERNO;
	}

	private CodigoVerificacion generarCodigo(Usuario usuario,
			TipoCodigoVerificacion tipo) {
		invalidarCodigosPrevios(usuario.getCorreo(), tipo);
		String codigo = String.format("%06d", random.nextInt(1_000_000));
		Instant expiraEn = Instant.now()
				.plus(minutosExpiracionCodigo, ChronoUnit.MINUTES);
		return codigoVerificacionRepository
				.save(new CodigoVerificacion(usuario, codigo, tipo, expiraEn));
	}

	private void invalidarCodigosPrevios(String correo, TipoCodigoVerificacion tipo) {
		List<CodigoVerificacion> activos = codigoVerificacionRepository
				.findAllByCorreoAndTipo(correo, tipo).stream()
				.filter(codigo -> !codigo.isUsado())
				.toList();
		if (activos.isEmpty()) {
			return;
		}
		activos.forEach(codigo -> codigo.setUsado(true));
		codigoVerificacionRepository.saveAll(activos);
	}

	private CodigoVerificacion buscarCodigoValido(String correo,
			TipoCodigoVerificacion tipo, String valor) {
		CodigoVerificacion codigo = codigoVerificacionRepository
				.findTopByCorreoAndTipoAndUsadoFalseOrderByCreadoEnDesc(correo, tipo)
				.orElseThrow(() -> new SolicitudInvalidaException(
						"El código no es válido o ya expiró."));
		if (codigo.getExpiraEn() == null
				|| codigo.getExpiraEn().isBefore(Instant.now())) {
			throw new SolicitudInvalidaException("El código no es válido o ya expiró.");
		}
		if (!codigo.getCodigo().equals(valor)) {
			throw new SolicitudInvalidaException("El código no es válido o ya expiró.");
		}
		return codigo;
	}

	private Usuario buscarUsuario(String correo) {
		return usuarioRepository.findByCorreoIgnoreCase(correo)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El usuario no existe."));
	}

	private String normalizarCorreo(String correo) {
		return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
	}
}
