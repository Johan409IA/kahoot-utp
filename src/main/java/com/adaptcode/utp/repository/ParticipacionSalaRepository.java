package com.adaptcode.utp.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.ParticipacionSala;

public interface ParticipacionSalaRepository extends JpaRepository<ParticipacionSala, Long> {

	List<ParticipacionSala> findAllBySala_Id(Long salaId);

	boolean existsBySala_IdAndEstudiante_Id(Long salaId, Long estudianteId);
}
