package com.adaptcode.utp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.RetroalimentacionDocente;

public interface RetroalimentacionDocenteRepository
		extends JpaRepository<RetroalimentacionDocente, Long> {

	List<RetroalimentacionDocente> findAllByPregunta_Id(Long preguntaId);

	boolean existsByAlternativa_Id(Long alternativaId);
}
