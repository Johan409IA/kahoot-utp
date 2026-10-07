package com.adaptcode.utp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.AlternativaDocente;

public interface AlternativaDocenteRepository extends JpaRepository<AlternativaDocente, Long> {

	List<AlternativaDocente> findAllByPregunta_IdOrderByOrdenVisualAsc(Long preguntaId);

	boolean existsByPregunta_IdAndOrdenVisual(Long preguntaId, int ordenVisual);
}
