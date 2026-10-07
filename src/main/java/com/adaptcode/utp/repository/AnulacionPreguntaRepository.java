package com.adaptcode.utp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.AnulacionPregunta;

public interface AnulacionPreguntaRepository extends JpaRepository<AnulacionPregunta, Long> {

	List<AnulacionPregunta> findAllBySala_Id(Long salaId);

	boolean existsBySala_IdAndPregunta_Id(Long salaId, Long preguntaId);
}
