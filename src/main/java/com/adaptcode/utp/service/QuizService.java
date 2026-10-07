package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.request.QuizRequest;
import com.adaptcode.utp.dto.response.QuizResponse;
import com.adaptcode.utp.model.EstadoQuiz;

public interface QuizService {

	QuizResponse crearQuiz(Long docenteId, QuizRequest request);

	QuizResponse mostrarPorId(Long docenteId, Long id);

	List<QuizResponse> listarPorDocente(Long docenteId, EstadoQuiz estado);

	QuizResponse actualizarQuiz(Long docenteId, Long id, QuizRequest request);

	void eliminarQuiz(Long docenteId, Long id);

	QuizResponse marcarQuizListo(Long docenteId, Long id);
}
