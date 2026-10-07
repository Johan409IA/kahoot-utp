package com.adaptcode.utp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.AlternativaDocenteRequest;
import com.adaptcode.utp.dto.response.AlternativaDocenteResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.AlternativaDocente;
import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.OrigenPregunta;
import com.adaptcode.utp.model.PreguntaDocente;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.AlternativaDocenteRepository;
import com.adaptcode.utp.repository.PreguntaDocenteRepository;
import com.adaptcode.utp.repository.QuizItemRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlternativaDocenteServiceImp implements AlternativaDocenteService {

	private final AlternativaDocenteRepository alternativaDocenteRepository;
	private final PreguntaDocenteRepository preguntaDocenteRepository;
	private final UsuarioRepository usuarioRepository;
	private final QuizItemRepository quizItemRepository;

	@Override
	@Transactional
	public AlternativaDocenteResponse crearAlternativa(
			Long docenteId, Long preguntaId, AlternativaDocenteRequest request) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		validarEditable(pregunta);
		if (alternativaDocenteRepository.existsByPregunta_IdAndOrdenVisual(
				preguntaId, request.ordenVisual())) {
			throw new SolicitudInvalidaException("El orden visual ya está en uso.");
		}
		AlternativaDocente alternativa = new AlternativaDocente(pregunta,
				request.texto(), request.codigo(), request.lenguaje(),
				request.ordenVisual(), request.correcta());
		return aResponse(alternativaDocenteRepository.save(alternativa));
	}

	@Override
	@Transactional(readOnly = true)
	public AlternativaDocenteResponse mostrarPorId(
			Long docenteId, Long preguntaId, Long id) {
		validarIdentificador(id);
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		AlternativaDocente alternativa = alternativaDocenteRepository.findById(id)
				.filter(a -> pregunta.getId().equals(a.getPregunta().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La alternativa no existe."));
		return aResponse(alternativa);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AlternativaDocenteResponse> listarPorPregunta(
			Long docenteId, Long preguntaId) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		return alternativaDocenteRepository
				.findAllByPregunta_IdOrderByOrdenVisualAsc(pregunta.getId()).stream()
				.map(this::aResponse)
				.toList();
	}

	@Override
	@Transactional
	public AlternativaDocenteResponse actualizarAlternativa(
			Long docenteId, Long preguntaId, Long id,
			AlternativaDocenteRequest request) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		validarEditable(pregunta);
		AlternativaDocente alternativa = alternativaDocenteRepository.findById(id)
				.filter(a -> pregunta.getId().equals(a.getPregunta().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La alternativa no existe."));
		if (alternativa.getOrdenVisual() != request.ordenVisual()
				&& alternativaDocenteRepository.existsByPregunta_IdAndOrdenVisual(
						preguntaId, request.ordenVisual())) {
			throw new SolicitudInvalidaException("El orden visual ya está en uso.");
		}
		alternativa.setTexto(request.texto());
		alternativa.setCodigo(request.codigo());
		alternativa.setLenguaje(request.lenguaje());
		alternativa.setOrdenVisual(request.ordenVisual());
		alternativa.setCorrecta(request.correcta());
		return aResponse(alternativaDocenteRepository.save(alternativa));
	}

	@Override
	@Transactional
	public void eliminarAlternativa(Long docenteId, Long preguntaId, Long id) {
		PreguntaDocente pregunta = buscarPreguntaPropia(docenteId, preguntaId);
		validarEditable(pregunta);
		AlternativaDocente alternativa = alternativaDocenteRepository.findById(id)
				.filter(a -> pregunta.getId().equals(a.getPregunta().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La alternativa no existe."));
		alternativaDocenteRepository.delete(alternativa);
	}

	private PreguntaDocente buscarPreguntaPropia(Long docenteId, Long preguntaId) {
		validarDocente(docenteId);
		validarIdentificador(preguntaId);
		return preguntaDocenteRepository.findById(preguntaId)
				.filter(p -> p.isActiva()
						&& docenteId.equals(p.getDocente().getId()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"La pregunta no existe."));
	}

	private void validarEditable(PreguntaDocente pregunta) {
		if (pregunta.getOrigen() == OrigenPregunta.INSTITUCIONAL) {
			throw new SolicitudInvalidaException(
					"La pregunta institucional no se puede modificar.");
		}
		if (quizItemRepository.existsByPregunta_IdAndQuiz_Estado(
				pregunta.getId(), EstadoQuiz.LISTO)) {
			throw new SolicitudInvalidaException(
					"La pregunta está usada en un quiz Listo y ya no se puede modificar.");
		}
	}

	private void validarDocente(Long docenteId) {
		validarIdentificador(docenteId);
		Usuario docente = usuarioRepository.findById(docenteId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El docente no existe."));
		if (docente.getRol() != RolUsuario.DOCENTE_UTP || !docente.isActivo()) {
			throw new SolicitudInvalidaException("El usuario no es un docente activo.");
		}
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private AlternativaDocenteResponse aResponse(AlternativaDocente alternativa) {
		return new AlternativaDocenteResponse(
				alternativa.getId(),
				alternativa.getPregunta().getId(),
				alternativa.getTexto(),
				alternativa.getCodigo(),
				alternativa.getLenguaje(),
				alternativa.getOrdenVisual(),
				alternativa.isCorrecta());
	}
}
