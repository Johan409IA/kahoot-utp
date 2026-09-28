package com.adaptcode.utp.config;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import com.adaptcode.utp.model.Curso;
import com.adaptcode.utp.repository.CursoRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(1)
@RequiredArgsConstructor
public class CatalogoInstitucionalInitializer implements CommandLineRunner {

	private static final List<String> CURSOS_INSTITUCIONALES = List.of(
			"Algoritmos",
			"Python",
			"Bases de Datos");

	private final CursoRepository cursoRepository;

	@Override
	public void run(String @NonNull ... args) {
		CURSOS_INSTITUCIONALES.stream()
				.filter(nombre -> !cursoRepository.existsByNombreIgnoreCase(nombre))
				.map(Curso::new)
				.forEach(cursoRepository::save);
	}
}
