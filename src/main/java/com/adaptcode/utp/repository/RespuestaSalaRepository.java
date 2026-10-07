package com.adaptcode.utp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.RespuestaSala;

public interface RespuestaSalaRepository extends JpaRepository<RespuestaSala, Long> {

	Optional<RespuestaSala> findByParticipacion_IdAndPregunta_Id(
			Long participacionId, Long preguntaId);

	List<RespuestaSala> findAllByParticipacion_Id(Long participacionId);

	boolean existsByParticipacion_IdAndPregunta_Id(Long participacionId, Long preguntaId);

	List<RespuestaSala> findAllByParticipacion_Sala_Id(Long salaId);
}
