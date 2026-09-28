package com.adaptcode.utp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByIdAndActivoTrue(Long id);

	Optional<Curso> findByNombreIgnoreCase(String nombre);

	List<Curso> findAllByActivoTrueOrderByNombreAsc();
}
