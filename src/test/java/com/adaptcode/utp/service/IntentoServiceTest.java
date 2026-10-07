package com.adaptcode.utp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.adaptcode.utp.dto.request.IntentoRequest;
import com.adaptcode.utp.model.Curso;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.ProgresoMicroReto;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.model.TipoMicroReto;
import com.adaptcode.utp.model.Usuario;
import com.adaptcode.utp.repository.InsigniaObtenidaRepository;
import com.adaptcode.utp.repository.IntentoRepository;
import com.adaptcode.utp.repository.MicroRetoRepository;
import com.adaptcode.utp.repository.ProgresoMicroRetoRepository;
import com.adaptcode.utp.repository.UsuarioRepository;

class IntentoServiceTest {

	private IntentoRepository intentoRepository;
	private UsuarioRepository usuarioRepository;
	private MicroRetoRepository microRetoRepository;
	private ProgresoMicroRetoRepository progresoMicroRetoRepository;
	private InsigniaObtenidaRepository insigniaObtenidaRepository;
	private IntentoServiceImp intentoService;

	@BeforeEach
	void configurar() {
		intentoRepository = mock(IntentoRepository.class);
		usuarioRepository = mock(UsuarioRepository.class);
		microRetoRepository = mock(MicroRetoRepository.class);
		progresoMicroRetoRepository = mock(ProgresoMicroRetoRepository.class);
		insigniaObtenidaRepository = mock(InsigniaObtenidaRepository.class);
		intentoService = new IntentoServiceImp(intentoRepository, usuarioRepository,
				microRetoRepository, progresoMicroRetoRepository,
				insigniaObtenidaRepository);
	}

	@Test
	void registrarAciertoSinAyuda_creaProgresoConPuntoYSegundoNoDuplica() {
		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(9L);
		when(usuario.isActivo()).thenReturn(true);
		Curso curso = new Curso("Algoritmos");
		ReflectionTestUtils.setField(curso, "id", 1L);
		Tema tema = new Tema(curso, "Condicionales");
		ReflectionTestUtils.setField(tema, "id", 10L);
		MicroReto reto = new MicroReto("DEMO-ALG-01", tema,
				TipoMicroReto.SELECCION_MULTIPLE, NivelMicroReto.BASICO,
				"Evalúa una condición.", null, null);
		ReflectionTestUtils.setField(reto, "id", 30L);
		when(usuarioRepository.findById(9L)).thenReturn(Optional.of(usuario));
		when(microRetoRepository.findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(
				30L)).thenReturn(Optional.of(reto));
		when(intentoRepository.countByUsuario_IdAndMicroReto_Id(9L, 30L))
				.thenReturn(0L, 1L);
		when(intentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(progresoMicroRetoRepository.findByUsuario_IdAndMicroReto_Id(9L, 30L))
				.thenReturn(Optional.empty())
				.thenAnswer(inv -> Optional.of(progresoGuardado(usuario, reto)));
		when(progresoMicroRetoRepository.save(any()))
				.thenAnswer(inv -> inv.getArgument(0));

		var primero = intentoService.registrarIntento(9L,
				new IntentoRequest(30L, true, false, 5000L));
		var segundo = intentoService.registrarIntento(9L,
				new IntentoRequest(30L, true, false, 4000L));

		assertEquals(1, primero.nroIntento());
		assertEquals(2, segundo.nroIntento());
		ArgumentCaptor<ProgresoMicroReto> captor = ArgumentCaptor
				.forClass(ProgresoMicroReto.class);
		verify(progresoMicroRetoRepository, times(1)).save(captor.capture());
		assertTrue(captor.getValue().isPuntoDominio());
	}

	private ProgresoMicroReto progresoGuardado(Usuario usuario, MicroReto reto) {
		ProgresoMicroReto progreso = new ProgresoMicroReto(usuario, reto);
		progreso.setCompletado(true);
		progreso.setPuntoDominio(true);
		return progreso;
	}
}
