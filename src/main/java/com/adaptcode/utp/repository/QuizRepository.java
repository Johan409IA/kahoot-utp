package com.adaptcode.utp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

	List<Quiz> findAllByDocente_IdOrderByIdDesc(Long docenteId);

	List<Quiz> findAllByDocente_IdAndEstadoOrderByIdDesc(Long docenteId, EstadoQuiz estado);

	@Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.items i LEFT JOIN FETCH i.pregunta WHERE q.id = :id AND q.docente.id = :docenteId")
	java.util.Optional<Quiz> buscarConItemsPorIdYDocente(
			@Param("id") Long id, @Param("docenteId") Long docenteId);
}
