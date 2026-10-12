package com.adaptcode.utp.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Configuración de seguridad: API stateless con JWT, contraseñas BCrypt y
 * autorización por rol. El catálogo y el alta/inicio de sesión son públicos.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	public static final String ROLE_DOCENTE = "DOCENTE_UTP";
	public static final String ROLE_ESTUDIANTE = "ESTUDIANTE_UTP";

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/cursos/**",
								"/api/micro-retos/**").permitAll()
						.requestMatchers("/api/docentes/**").hasRole(ROLE_DOCENTE)
						.requestMatchers(HttpMethod.POST,
								"/api/salas/*/participaciones/*/expulsar",
								"/api/salas/*/participaciones/*/readmitir")
						.hasRole(ROLE_DOCENTE)
						.requestMatchers(HttpMethod.GET, "/api/salas/*/ranking")
						.hasAnyRole(ROLE_DOCENTE, ROLE_ESTUDIANTE)
						.requestMatchers(HttpMethod.GET, "/api/salas/*/participaciones/**",
								"/api/salas/*/respuestas/**")
						.hasAnyRole(ROLE_DOCENTE, ROLE_ESTUDIANTE)
						.requestMatchers("/api/salas/**").hasRole(ROLE_ESTUDIANTE)
						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(unauthorizedEntryPoint())
						.accessDeniedHandler(accessDeniedHandler()))
				.addFilterBefore(jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
			JwtAuthenticationFilter filter) {
		FilterRegistrationBean<JwtAuthenticationFilter> registration =
				new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	private AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, ex) -> escribirError(response,
				HttpServletResponse.SC_UNAUTHORIZED,
				"No autenticado. Debe iniciar sesión.");
	}

	private AccessDeniedHandler accessDeniedHandler() {
		return (request, response, ex) -> escribirError(response,
				HttpServletResponse.SC_FORBIDDEN,
				"Acceso denegado para su rol.");
	}

	private void escribirError(HttpServletResponse response, int status, String mensaje)
			throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write("{\"error\":\"" + mensaje + "\"}");
	}
}
