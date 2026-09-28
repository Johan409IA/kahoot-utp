package com.adaptcode.utp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.adaptcode.utp.dto.BloqueMicroRetoResponseDTO;
import com.adaptcode.utp.dto.CodigoMicroRetoResponseDTO;
import com.adaptcode.utp.dto.ContenidoMicroRetoResponseDTO;
import com.adaptcode.utp.dto.FragmentoMicroRetoResponseDTO;
import com.adaptcode.utp.dto.MicroRetoDetalleResponseDTO;
import com.adaptcode.utp.dto.MicroRetoResumenResponseDTO;
import com.adaptcode.utp.dto.OpcionMicroRetoResponseDTO;
import com.adaptcode.utp.dto.OrdenarBloquesContenidoDTO;
import com.adaptcode.utp.dto.PredecirSalidaContenidoDTO;
import com.adaptcode.utp.dto.SeleccionMultipleContenidoDTO;
import com.adaptcode.utp.dto.SeleccionarFragmentoContenidoDTO;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.TipoMicroReto;
import com.adaptcode.utp.service.MicroRetoService;

@WebMvcTest(MicroRetoController.class)
class MicroRetoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MicroRetoService microRetoService;

	@Test
	void listarMicroRetos_devuelveResumenesSinContenidoPrivado() throws Exception {
		when(microRetoService.obtenerResumenes(1L, null, NivelMicroReto.BASICO)).thenReturn(List.of(
				new MicroRetoResumenResponseDTO(20L, 10L, TipoMicroReto.SELECCION_MULTIPLE,
						NivelMicroReto.BASICO, "Evalúa una condición.")));

		mockMvc.perform(get("/api/micro-retos").param("cursoId", "1").param("nivel", "BASICO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].tipo").value("SELECCION_MULTIPLE"))
				.andExpect(jsonPath("$[0].enunciado").value("Evalúa una condición."))
				.andExpect(jsonPath("$[0].contenido").doesNotExist());
	}

	@Test
	void listarMicroRetos_devuelveListaVaciaCuandoNoHayCoincidencias() throws Exception {
		when(microRetoService.obtenerResumenes(1L, null, null)).thenReturn(List.of());

		mockMvc.perform(get("/api/micro-retos").param("cursoId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void obtenerDetalle_noExponeSiUnaOpcionEsCorrectaNiSuRetroalimentacion() throws Exception {
		ContenidoMicroRetoResponseDTO contenido = new SeleccionMultipleContenidoDTO(List.of(
				new OpcionMicroRetoResponseDTO(1L, "Verdadero"),
				new OpcionMicroRetoResponseDTO(2L, "Falso")));
		when(microRetoService.obtenerDetalle(20L)).thenReturn(detalle(
				TipoMicroReto.SELECCION_MULTIPLE, contenido));

		mockMvc.perform(get("/api/micro-retos/20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contenido.opciones[0].texto").value("Verdadero"))
				.andExpect(jsonPath("$.contenido.opciones[0].correcta").doesNotExist())
				.andExpect(jsonPath("$.contenido.retroalimentacion").doesNotExist());
	}

	@Test
	void obtenerDetalle_devuelveBloquesSinRevelarElOrdenCorrecto() throws Exception {
		ContenidoMicroRetoResponseDTO contenido = new OrdenarBloquesContenidoDTO(List.of(
				new BloqueMicroRetoResponseDTO(1L, "Leer n"),
				new BloqueMicroRetoResponseDTO(2L, "Evaluar n > 0")));
		when(microRetoService.obtenerDetalle(21L)).thenReturn(detalle(
				TipoMicroReto.ORDENAR_BLOQUES, contenido));

		mockMvc.perform(get("/api/micro-retos/21"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contenido.bloques[0].texto").value("Leer n"))
				.andExpect(jsonPath("$.contenido.bloques[0].ordenCorrecto").doesNotExist());
	}

	@Test
	void obtenerDetalle_devuelveCodigoYOpcionesParaPredecirSalida() throws Exception {
		ContenidoMicroRetoResponseDTO contenido = new PredecirSalidaContenidoDTO(
				new CodigoMicroRetoResponseDTO("python", "print(2 + 3 * 2)"),
				List.of(new OpcionMicroRetoResponseDTO(1L, "8"),
						new OpcionMicroRetoResponseDTO(2L, "10")));
		when(microRetoService.obtenerDetalle(22L)).thenReturn(detalle(
				TipoMicroReto.PREDECIR_SALIDA, contenido));

		mockMvc.perform(get("/api/micro-retos/22"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contenido.codigo.codigo").value("print(2 + 3 * 2)"))
				.andExpect(jsonPath("$.contenido.opciones[0].texto").value("8"));
	}

	@Test
	void obtenerDetalle_devuelveFragmentosDeCodigoSinExponerLaCorrecta() throws Exception {
		ContenidoMicroRetoResponseDTO contenido = new SeleccionarFragmentoContenidoDTO(List.of(
				new FragmentoMicroRetoResponseDTO(1L, "SELECT * FROM estudiantes;", "sql"),
				new FragmentoMicroRetoResponseDTO(2L, "DELETE FROM estudiantes;", "sql")));
		when(microRetoService.obtenerDetalle(23L)).thenReturn(detalle(
				TipoMicroReto.SELECCIONAR_FRAGMENTO, contenido));

		mockMvc.perform(get("/api/micro-retos/23"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contenido.opciones[0].codigo").value("SELECT * FROM estudiantes;"))
				.andExpect(jsonPath("$.contenido.opciones[0].correcta").doesNotExist());
	}

	@Test
	void listarMicroRetos_devuelve400CuandoElNivelNoEsValido() throws Exception {
		mockMvc.perform(get("/api/micro-retos").param("cursoId", "1").param("nivel", "EXPERTO"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void obtenerDetalle_devuelve404CuandoElRetoNoExiste() throws Exception {
		when(microRetoService.obtenerDetalle(999L)).thenThrow(new ResponseStatusException(
				org.springframework.http.HttpStatus.NOT_FOUND));

		mockMvc.perform(get("/api/micro-retos/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void crearMicroReto_devuelve405PorqueEstaEtapaSoloPermiteConsultas() throws Exception {
		mockMvc.perform(post("/api/micro-retos"))
				.andExpect(status().isMethodNotAllowed());
	}

	private MicroRetoDetalleResponseDTO detalle(
			TipoMicroReto tipo, ContenidoMicroRetoResponseDTO contenido) {
		return new MicroRetoDetalleResponseDTO(20L, 10L, tipo, NivelMicroReto.BASICO,
				"Enunciado de demostración.", contenido);
	}
}
