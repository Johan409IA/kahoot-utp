package com.adaptcode.utp.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.adaptcode.utp.model.RolUsuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida tokens JWT (firma HS256 + expiración). El correo es el
 * subject y se agregan como claims el identificador y el rol del usuario.
 */
@Component
public class JwtService {

	private final Key key;
	private final long expirationMs;
	private final String emitter;

	public JwtService(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration}") long expirationMs,
			@Value("${jwt.emitter:adaptcode-utp-api}") String emitter) {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException(
					"jwt.secret debe tener al menos 32 caracteres para HS256.");
		}
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
		this.emitter = emitter;
	}

	public String generateToken(UsuarioPrincipal principal) {
		Instant ahora = Instant.now();
		Instant expiracion = ahora.plusMillis(expirationMs);
		return Jwts.builder()
				.setSubject(principal.getCorreo())
				.setIssuer(emitter)
				.claim("id", principal.getId())
				.claim("rol", principal.getRol().name())
				.setIssuedAt(Date.from(ahora))
				.setExpiration(Date.from(expiracion))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public Long extractId(String token) {
		Object id = parseClaims(token).get("id");
		return id instanceof Number numero ? numero.longValue() : null;
	}

	public RolUsuario extractRol(String token) {
		String rol = parseClaims(token).get("rol", String.class);
		return rol == null ? null : RolUsuario.valueOf(rol);
	}

	public Instant extractIssuedAt(String token) {
		Date emitido = parseClaims(token).getIssuedAt();
		return emitido == null ? null : emitido.toInstant();
	}

	public Instant extractExpiration(String token) {
		Date expiracion = parseClaims(token).getExpiration();
		return expiracion == null ? null : expiracion.toInstant();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			if (!userDetails.isEnabled()) {
				return false;
			}
			Claims claims = parseClaims(token);
			String correo = claims.getSubject();
			if (correo == null || !correo.equalsIgnoreCase(userDetails.getUsername())) {
				return false;
			}
			Date expiracion = claims.getExpiration();
			if (expiracion == null || expiracion.before(new Date())) {
				return false;
			}
			if (userDetails instanceof UsuarioPrincipal principal) {
				Instant cambio = principal.getCredencialCambiadaEn();
				if (cambio != null && claims.getIssuedAt() != null
						&& claims.getIssuedAt().toInstant().isBefore(cambio)) {
					return false;
				}
			}
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}
