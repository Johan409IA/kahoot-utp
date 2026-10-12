package com.adaptcode.utp.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.dto.request.SalaRequest;
import com.adaptcode.utp.dto.response.SalaResponse;
import com.adaptcode.utp.exception.ResourceNotFoundException;
import com.adaptcode.utp.exception.SolicitudInvalidaException;
import com.adaptcode.utp.model.EstadoSala;
import com.adaptcode.utp.model.EstadoQuiz;
import com.adaptcode.utp.model.EstadoSala;
import com.adaptcode.utp.model.Quiz;
import com.adaptcode.utp.model.RolUsuario;
import com.adaptcode.utp.model.Sala;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.QuizRepository;
import com.adaptcode.utp.repository.SalaRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalaServiceImp implements SalaService {

	private static final Set<Integer> TIEMPOS_VALIDOS = Set.of(20, 30, 60, 90, 120);

	private final SalaRepository salaRepository;
	private final UsuarioRepository usuarioRepository;
	private final QuizRepository quizRepository;
	private final SecureRandom pinAleatorio = new SecureRandom();

	@Override
	@Transactional
	public SalaResponse crearSala(Long docenteId, SalaRequest request) {
		Usuario docente = validarDocente(docenteId);
		Quiz quiz = quizRepository.buscarConItemsPorIdYDocente(request.quizId(), docenteId)
				.orElseThrow(() -> new ResourceNotFoundException("El quiz no existe."));
		if (quiz.getEstado() != EstadoQuiz.LISTO) {
			throw new SolicitudInvalidaException(
					"Solo un quiz Listo para usar puede originar una sala.");
		}
		if (!TIEMPOS_VALIDOS.contains(request.tiempoPorPregunta())) {
			throw new SolicitudInvalidaException(
					"El tiempo por pregunta debe ser 20, 30, 60, 90 o 120 segundos.");
		}
		Sala sala = new Sala(docente, quiz, generarPinUnico(), request.tiempoPorPregunta());
		sala.setAmpliacion(request.ampliacion());
		sala.setQuizVersionCongeladaEn(java.time.Instant.now());
		return aResponse(salaRepository.save(sala));
	}

	@Override
	@Transactional(readOnly = true)
	public SalaResponse mostrarPorId(Long docenteId, Long id) {
		validarIdentificador(id);
		validarDocente(docenteId);
		Sala sala = salaRepository.findById(id)
				.filter(s -> docenteId.equals(s.getDocente().getId()))
				.orElseThrow(() -> new ResourceNotFoundException("La sala no existe."));
		return aResponse(sala);
	}

	@Override
	@Transactional(readOnly = true)
	public SalaResponse mostrarPorPin(String pin) {
		if (pin == null || !pin.matches("\\d{6}")) {
			throw new SolicitudInvalidaException(
					"El PIN debe tener seis dígitos numéricos.");
		}
		Sala sala = salaRepository.findByPin(pin)
				.orElseThrow(() -> new ResourceNotFoundException(
						"La sala no existe."));
		return aResponse(sala);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SalaResponse> listarPorDocente(Long docenteId, EstadoSala estado) {
		validarDocente(docenteId);
		List<Sala> salas = estado == null
				? salaRepository.findAllByDocente_IdOrderByIdDesc(docenteId)
				: salaRepository.buscarPorDocenteYEstado(docenteId, estado);
		return salas.stream()
				.map(this::aResponse)
				.toList();
	}

	@Override
	@Transactional
	public SalaResponse abrirEspera(Long docenteId, Long id) {
		Sala sala = buscarSalaDeDocente(docenteId, id);
		if (sala.getEstado() != EstadoSala.BORRADOR) {
			throw new SolicitudInvalidaException(
					"Solo una sala en borrador puede abrir la espera.");
		}
		sala.setEstado(EstadoSala.SALA_ESPERA);
		return aResponse(salaRepository.save(sala));
	}

	@Override
	@Transactional
	public SalaResponse iniciarSala(Long docenteId, Long id) {		Sala sala = buscarSalaDeDocente(docenteId, id);
		if (sala.getEstado() != EstadoSala.BORRADOR
				&& sala.getEstado() != EstadoSala.SALA_ESPERA) {
			throw new SolicitudInvalidaException(
					"Solo una sala en borrador o espera se puede iniciar.");
		}
		if (sala.getQuizOrigen().getEstado() != EstadoQuiz.LISTO) {
			throw new SolicitudInvalidaException(
					"Solo un quiz Listo para usar puede originar una sala.");
		}
		sala.setEstado(EstadoSala.EN_CURSO);
		if (sala.getQuizVersionCongeladaEn() == null) {
			sala.setQuizVersionCongeladaEn(java.time.Instant.now());
		}
		return aResponse(salaRepository.save(sala));
	}

	@Override
	@Transactional
	public SalaResponse pausarSala(Long docenteId, Long id) {
		Sala sala = buscarSalaDeDocente(docenteId, id);
		if (sala.getEstado() != EstadoSala.EN_CURSO) {
			throw new SolicitudInvalidaException(
					"Solo una sala en curso se puede pausar.");
		}
		sala.setEstado(EstadoSala.PAUSADA);
		return aResponse(salaRepository.save(sala));
	}

	@Override
	@Transactional
	public SalaResponse reanudarSala(Long docenteId, Long id) {
		Sala sala = buscarSalaDeDocente(docenteId, id);
		if (sala.getEstado() != EstadoSala.PAUSADA) {
			throw new SolicitudInvalidaException(
					"Solo una sala pausada se puede reanudar.");
		}
		sala.setEstado(EstadoSala.EN_CURSO);
		return aResponse(salaRepository.save(sala));
	}

	@Override
	@Transactional
	public SalaResponse finalizarSala(Long docenteId, Long id) {
		Sala sala = buscarSalaDeDocente(docenteId, id);
		if (sala.getEstado() != EstadoSala.EN_CURSO
				&& sala.getEstado() != EstadoSala.PAUSADA) {
			throw new SolicitudInvalidaException(
					"Solo una sala en curso o pausada se puede finalizar.");
		}
		sala.setEstado(EstadoSala.FINALIZADA);
		return aResponse(salaRepository.save(sala));
	}

	private Sala buscarSalaDeDocente(Long docenteId, Long id) {
		validarDocente(docenteId);
		validarIdentificador(id);
		Sala sala = salaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"La sala no existe."));
		if (!docenteId.equals(sala.getDocente().getId())) {
			throw new ResourceNotFoundException("La sala no existe.");
		}
		return sala;
	}

	private String generarPinUnico() {
		for (int intento = 0; intento < 20; intento++) {
			String pin = String.format("%06d", pinAleatorio.nextInt(1000000));
			if (!salaRepository.existsByPin(pin)) {
				return pin;
			}
		}
		throw new SolicitudInvalidaException("No se pudo generar un PIN único.");
	}

	private Usuario validarDocente(Long docenteId) {
		validarIdentificador(docenteId);
		Usuario docente = usuarioRepository.findById(docenteId)
				.orElseThrow(() -> new ResourceNotFoundException("El docente no existe."));
		if (docente.getRol() != RolUsuario.DOCENTE_UTP || !docente.isActivo()) {
			throw new SolicitudInvalidaException("El usuario no es un docente activo.");
		}
		return docente;
	}

	private void validarIdentificador(Long id) {
		if (id == null || id <= 0) {
			throw new SolicitudInvalidaException("El identificador debe ser positivo.");
		}
	}

	private SalaResponse aResponse(Sala sala) {
		return new SalaResponse(
				sala.getId(),
				sala.getDocente().getId(),
				sala.getQuizOrigen().getId(),
				sala.getPin(),
				sala.getEstado(),
				sala.getTiempoPorPregunta(),
				sala.getAmpliacion(),
				sala.getQuizVersionCongeladaEn());
	}
}
