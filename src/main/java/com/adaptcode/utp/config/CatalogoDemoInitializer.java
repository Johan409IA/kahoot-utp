package com.adaptcode.utp.config;



import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.adaptcode.utp.model.Alternativa;
import com.adaptcode.utp.model.Bloque;
import com.adaptcode.utp.model.Curso;
import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.NivelMicroReto;
import com.adaptcode.utp.model.Retroalimentacion;
import com.adaptcode.utp.model.Tema;
import com.adaptcode.utp.model.TipoMicroReto;
import com.adaptcode.utp.repository.CursoRepository;
import com.adaptcode.utp.repository.MicroRetoRepository;
import com.adaptcode.utp.repository.TemaRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("demo")
@Order(2)
@RequiredArgsConstructor
public class CatalogoDemoInitializer implements CommandLineRunner {

	private final CursoRepository cursoRepository;
	private final TemaRepository temaRepository;
	private final MicroRetoRepository microRetoRepository;

	@Override
	@Transactional
	public void run(String @NonNull ... args) {
		Curso algoritmos = obtenerCurso("Algoritmos");
		Curso python = obtenerCurso("Python");
		Curso basesDeDatos = obtenerCurso("Bases de Datos");

		Tema condicionales = obtenerTema(algoritmos, "Condicionales");
		Tema secuencias = obtenerTema(algoritmos, "Secuencias");
		Tema expresiones = obtenerTema(python, "Expresiones");
		Tema consultasSelect = obtenerTema(basesDeDatos, "Consultas SELECT");

		crearDemoSiFalta(microRetoCondicional(condicionales));
		crearDemoSiFalta(microRetoOrdenarSecuencia(secuencias));
		crearDemoSiFalta(microRetoPredecirSalida(expresiones));
		crearDemoSiFalta(microRetoSeleccionarConsulta(consultasSelect));
	}

	private Curso obtenerCurso(String nombre) {
		return cursoRepository.findByNombreIgnoreCase(nombre)
				.orElseThrow(() -> new IllegalStateException("Falta el curso institucional " + nombre + "."));
	}

	private Tema obtenerTema(Curso curso, String nombre) {
		return temaRepository.findByCurso_IdAndNombreIgnoreCase(curso.getId(), nombre)
				.orElseGet(() -> temaRepository.save(new Tema(curso, nombre)));
	}

	private void crearDemoSiFalta(MicroReto microReto) {
		if (!microRetoRepository.existsByCodigoIgnoreCase(microReto.getCodigo())) {
			microRetoRepository.save(microReto);
		}
	}

	private MicroReto microRetoCondicional(Tema tema) {
		MicroReto reto = nuevoReto("DEMO-ALG-COND-01", tema, TipoMicroReto.SELECCION_MULTIPLE,
				"Con edad = 18, ¿la condición edad >= 18 es verdadera?", null, null);
		Alternativa verdadera = agregarOpcion(reto, "Verdadero", 1, true);
		Alternativa falsa = agregarOpcion(reto, "Falso", 2, false);
		agregarFeedback(reto, null,
				"La condición incluye el caso límite.",
				">= acepta valores mayores o iguales. Como edad vale 18, la condición se cumple.",
				"Comparar 18 con 18; comprobar que >= incluye igualdad; obtener verdadero.",
				"Distingue > de >= en el límite de la condición.");
		agregarFeedback(reto, falsa,
				"Se interpretó >= como si excluyera la igualdad.",
				">= acepta valores mayores o iguales. Como edad vale 18, la condición se cumple.",
				"Comparar 18 con 18; comprobar que >= incluye igualdad; obtener verdadero.",
				"Distingue > de >= en el límite de la condición.");
		return reto;
	}

	private MicroReto microRetoOrdenarSecuencia(Tema tema) {
		MicroReto reto = nuevoReto("DEMO-ALG-SEC-01", tema, TipoMicroReto.ORDENAR_BLOQUES,
				"Ordena los pasos para mostrar si un número es positivo.", null, null);
		reto.agregarBloque(new Bloque("Mostrar positivo", 1, 3));
		reto.agregarBloque(new Bloque("Leer n", 2, 1));
		reto.agregarBloque(new Bloque("Evaluar si n > 0", 3, 2));
		agregarFeedback(reto, null,
				"Se intentó decidir antes de leer el dato o mostrar el resultado antes de evaluar.",
				"La secuencia necesita primero el valor, luego la condición y al final la acción.",
				"Leer n; evaluar n > 0; mostrar el resultado de esa evaluación.",
				"Ubica primero la entrada y después la decisión.");
		return reto;
	}

	private MicroReto microRetoPredecirSalida(Tema tema) {
		MicroReto reto = nuevoReto("DEMO-PY-EXPR-01", tema, TipoMicroReto.PREDECIR_SALIDA,
				"¿Qué valor imprime este fragmento de Python?", "print(2 + 3 * 2)", "python");
		agregarOpcion(reto, "8", 1, true);
		Alternativa diez = agregarOpcion(reto, "10", 2, false);
		Alternativa siete = agregarOpcion(reto, "7", 3, false);
		agregarFeedback(reto, null,
				"La multiplicación se evalúa antes que la suma.",
				"3 * 2 produce 6 y después se suma el 2 inicial; por eso se imprime 8.",
				"Calcular 3 * 2 = 6; calcular 2 + 6 = 8.",
				"Aplica la precedencia de operadores antes de sumar.");
		agregarFeedback(reto, diez,
				"Se sumó antes de multiplicar.",
				"La multiplicación tiene prioridad: 3 * 2 produce 6, y después se suma 2.",
				"Calcular 3 * 2 = 6; calcular 2 + 6 = 8.",
				"Aplica la precedencia de operadores antes de sumar.");
		agregarFeedback(reto, siete,
				"Se omitió el valor inicial de la suma.",
				"La expresión incluye el 2 que aparece antes del operador +.",
				"Calcular 3 * 2 = 6; sumar el 2 inicial para obtener 8.",
				"Evalúa cada operando de izquierda a derecha respetando la precedencia.");
		return reto;
	}

	private MicroReto microRetoSeleccionarConsulta(Tema tema) {
		MicroReto reto = nuevoReto("DEMO-BD-SELECT-01", tema, TipoMicroReto.SELECCIONAR_FRAGMENTO,
				"¿Qué consulta obtiene todas las filas de la tabla estudiantes?", null, null);
		agregarFragmento(reto, "SELECT * FROM estudiantes;", 1, true);
		Alternativa selectIncorrecto = agregarFragmento(reto, "SELECT estudiantes FROM *;", 2, false);
		Alternativa delete = agregarFragmento(reto, "DELETE FROM estudiantes;", 3, false);
		agregarFeedback(reto, null,
				"SELECT consulta filas sin modificarlas.",
				"SELECT * devuelve todas las columnas de la tabla indicada después de FROM.",
				"Interpretar * como todas las columnas; interpretar FROM estudiantes como origen de las filas.",
				"Revisa qué significa el asterisco en una consulta SELECT.");
		agregarFeedback(reto, selectIncorrecto,
				"El nombre de la tabla se colocó en la lista de columnas.",
				"En SELECT se indican las columnas antes de FROM y el nombre de la tabla después.",
				"Escribir SELECT y las columnas; agregar FROM y luego la tabla.",
				"Identifica qué parte nombra las columnas y cuál nombra la tabla.");
		agregarFeedback(reto, delete,
				"Se eligió una instrucción que elimina datos.",
				"DELETE modifica la tabla; para consultar filas se utiliza SELECT.",
				"Usar SELECT para leer; reservar DELETE para eliminar filas con una condición.",
				"Comprueba si el verbo SQL consulta o modifica información.");
		return reto;
	}

	private MicroReto nuevoReto(String codigo, Tema tema, TipoMicroReto tipo,
			String enunciado, String codigoBase, String lenguaje) {
		return new MicroReto(codigo, tema, tipo, NivelMicroReto.BASICO, enunciado, codigoBase, lenguaje);
	}

	private Alternativa agregarOpcion(MicroReto reto, String texto, int orden, boolean correcta) {
		Alternativa alternativa = new Alternativa(texto, null, null, orden, correcta);
		reto.agregarAlternativa(alternativa);
		return alternativa;
	}

	private Alternativa agregarFragmento(MicroReto reto, String codigo, int orden, boolean correcta) {
		Alternativa alternativa = new Alternativa(null, codigo, "sql", orden, correcta);
		reto.agregarAlternativa(alternativa);
		return alternativa;
	}

	private void agregarFeedback(MicroReto reto, Alternativa alternativa, String causa,
			String explicacion, String secuenciaTextual, String sugerencia) {
		reto.agregarRetroalimentacion(new Retroalimentacion(
				alternativa, causa, explicacion, secuenciaTextual, sugerencia));
	}
}
