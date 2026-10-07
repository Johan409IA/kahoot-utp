package com.adaptcode.utp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.QuizItem;

public interface QuizItemRepository extends JpaRepository<QuizItem, Long> {

	boolean existsByQuiz_IdAndPregunta_Id(Long quizId, Long preguntaId);

	boolean existsByPregunta_IdAndQuiz_Estado(Long preguntaId, EstadoQuiz estado);
}
