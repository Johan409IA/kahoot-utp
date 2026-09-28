package com.adaptcode.utp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.Tema;

public interface TemaRepository extends JpaRepository<Tema, Long> {

	List<Tema> findAllByCurso_IdAndActivoTrueOrderByNombreAsc(Long cursoId);

	Optional<Tema> findByCurso_IdAndNombreIgnoreCase(Long cursoId, String nombre);

	Optional<Tema> findByIdAndActivoTrue(Long id);
}
