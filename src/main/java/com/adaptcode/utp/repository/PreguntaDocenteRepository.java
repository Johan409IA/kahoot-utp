package com.adaptcode.utp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adaptcode.utp.model.OrigenPregunta;
import com.adaptcode.utp.model.PreguntaDocente;

public interface PreguntaDocenteRepository extends JpaRepository<PreguntaDocente, Long> {

	List<PreguntaDocente> findAllByDocente_IdAndActivaTrueOrderByIdAsc(Long docenteId);
	// ! CONSULTA PARA OBTENER LAS PREGUNTAS PROPIAS DE UN DOCENTE POR ORIGEN
	@Query("SELECT p FROM PreguntaDocente p JOIN FETCH p.tema t WHERE p.docente.id = :docenteId AND p.origen = :origen AND p.activa = TRUE ORDER BY p.id ASC")
	List<PreguntaDocente> buscarPropiasPorDocenteYOrigen(
			@Param("docenteId") Long docenteId, @Param("origen") OrigenPregunta origen);
}
