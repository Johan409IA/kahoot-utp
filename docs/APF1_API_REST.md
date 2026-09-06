# APF1 — API REST de DevKahoot

## Objetivo y alcance

Este Avance de Proyecto Final 1 (APF1) implementa una API REST con **Spring Boot 4.1.1** y **Java 25** que replica el flujo básico de Kahoot para estudiantes de Ingeniería de Sistemas: un docente registra preguntas, crea una partida, los estudiantes ingresan con un código y un apodo, responden y reciben un resultado con retroalimentación.

**Alcance:**
- Arquitectura por capas `model` → `service` → `controller`.
- Almacenamiento en memoria con `ArrayList` (sin base de datos ni JPA).
- Inyección de dependencias por constructor con `@Service` y `@RestController`.
- Exactamente seis pruebas automatizadas que cubren las funcionalidades principales del avance.

**Fuera de alcance:** base de datos, seguridad/autenticación, frontend y preguntas aleatorias (`obtenerPreguntaAleatoria()` no existe; las preguntas conservan su orden de registro).

## Diagrama de arquitectura

```
Cliente → Controller (@RestController) → Service (@Service) → ArrayList (memoria)
                          HTTP / JSON                 lógica de negocio      almacenamiento volátil
```

La petición HTTP llega al controlador, que valida códigos de respuesta y delega en el servicio. El servicio hace búsquedas, validaciones, CRUD y evaluación de respuestas sobre las listas en memoria. No hay capa `repository` porque todavía no existe persistencia.

## Modelos del dominio

| Modelo | Campos | Descripción |
|---|---|---|
| `Pregunta` | `id`, `enunciado`, `opciones`, `respuestaCorrecta`, `explicacion`, `categoria`, `orden` | Pregunta de selección múltiple usada para administrar el banco de preguntas. `orden` refleja el orden de registro y no cambia al actualizar. |
| `PreguntaParaEstudiante` | `id`, `enunciado`, `opciones`, `categoria`, `orden` | Versión pública de la pregunta que recibe el estudiante al consultar `GET /api/partidas/{codigo}/preguntas`. No incluye `respuestaCorrecta` ni `explicacion`. |
| `Estudiante` | `id`, `apodo`, `codigoPartida`, `puntaje` | Jugador identificado por apodo dentro de una partida. |
| `Partida` | `codigo`, `nombre`, `estado`, `idsPreguntas`, `idsEstudiantes` | Sala de juego identificada por `codigo`; guarda referencias por id a preguntas y estudiantes. Todos los ids de `idsPreguntas` deben existir al crearla. |
| `Respuesta` | `estudianteId`, `preguntaId`, `opcionSeleccionada` | Respuesta enviada por un estudiante a una pregunta. Solo se acepta la primera respuesta de cada estudiante por pregunta. |
| `ResultadoRespuesta` | `correcta`, `puntosObtenidos`, `retroalimentacion` | Resultado de evaluar una respuesta: 100 puntos si es correcta, 0 si no. |
| `EstadoPartida` (enum) | `ESPERANDO`, `EN_CURSO`, `FINALIZADA` | Ciclo de vida de la partida. Solo se aceptan respuestas si está `EN_CURSO`. |

**¿Qué significa `idsPreguntas`?** Es una lista de números que contiene los `id` de preguntas que ya fueron creadas mediante `POST /api/preguntas`. Por ejemplo, si existen las preguntas con ids `1` y `2`, una partida puede usar `"idsPreguntas": [1, 2]`. La API valida que todos esos ids existan; si alguno no existe, rechaza la partida con `400 Bad Request`. La partida no copia las preguntas: guarda sus referencias y las devuelve en ese mismo orden mediante `GET /api/partidas/{codigo}/preguntas`. Primero se crean todas las preguntas y luego se crea la partida usando sus ids. Una pregunta asociada a una partida no puede eliminarse; el DELETE responde `409 Conflict` para proteger la integridad de la sala.

**Relaciones:** una `Partida` contiene muchos `Pregunta` (por `idsPreguntas`) y muchos `Estudiante` (por `idsEstudiantes`). Una `Respuesta` une un `Estudiante` y una `Pregunta` dentro de una `Partida`, y produce un `ResultadoRespuesta`.

## Reglas de negocio principales

- **Preguntas públicas vs. preguntas completas:** el CRUD de `/api/preguntas` devuelve la `Pregunta` completa (incluye `respuestaCorrecta` y `explicacion`) porque está orientado al docente. El endpoint `GET /api/partidas/{codigo}/preguntas` está orientado al estudiante y devuelve `PreguntaParaEstudiante`, sin la solución ni la explicación, para que el participante no reciba la respuesta correcta antes de responder.
- **Una sola respuesta por pregunta:** dentro de una partida, un estudiante solo puede responder una vez cada pregunta. La primera respuesta se registra y suma puntos (100 si es correcta, 0 si no). Un intento repetido del mismo estudiante sobre la misma pregunta responde `409 Conflict`, no se registra en la lista de respuestas y no modifica el puntaje.
- **Orden de preguntas:** al crear una pregunta, `orden` se calcula como el máximo `orden` existente más uno. Esto evita órdenes duplicados cuando se elimina una pregunta intermedia y luego se crea otra. La actualización de una pregunta conserva su `orden` actual.
- **Estado de la partida:** solo se aceptan respuestas cuando la partida está `EN_CURSO`.

## Tabla de endpoints

### CRUD de preguntas — `/api/preguntas`

| Método | Ruta | Método Java | Cuerpo de solicitud | Códigos HTTP |
|---|---|---|---|---|
| GET | `/api/preguntas` | `obtenerTodasLasPreguntas()` | — | 200 |
| GET | `/api/preguntas/{id}` | `obtenerPreguntaPorId(int id)` | — | 200, 404 |
| POST | `/api/preguntas` | `agregarNuevaPregunta(Pregunta pregunta)` | JSON de `Pregunta` | 201, 400 |
| PUT | `/api/preguntas/{id}` | `actualizarPregunta(int id, Pregunta nuevaPregunta)` | JSON de `Pregunta` | 200, 400, 404 |
| DELETE | `/api/preguntas/{id}` | `eliminarPregunta(int id)` | — | 204, 404, 409 |

### Estudiantes — `/api/estudiantes`

| Método | Ruta | Método Java | Cuerpo de solicitud | Códigos HTTP |
|---|---|---|---|---|
| GET | `/api/estudiantes` | `obtenerTodosLosEstudiantes()` | — | 200 |
| GET | `/api/estudiantes/{id}` | `obtenerEstudiantePorId(int id)` | — | 200, 404 |
| PUT | `/api/estudiantes/{id}` | `actualizarEstudiante(int id, Estudiante estudiante)` | JSON de `Estudiante` | 200, 400, 404 |
| DELETE | `/api/estudiantes/{id}` | `eliminarEstudiante(int id)` | — | 204, 404 |

### Partidas — `/api/partidas`

| Método | Ruta | Método Java | Cuerpo de solicitud | Códigos HTTP |
|---|---|---|---|---|
| POST | `/api/partidas` | `crearNuevaPartida(Partida partida)` | JSON de `Partida` | 201, 400, 409 |
| GET | `/api/partidas/{codigo}` | `obtenerPartidaPorCodigo(String codigo)` | — | 200, 404 |
| PUT | `/api/partidas/{codigo}/estado` | `actualizarEstadoPartida(String codigo, EstadoPartida estado)` | `"EN_CURSO"` | 200, 400, 404 |
| GET | `/api/partidas/{codigo}/preguntas` | `obtenerPreguntasDePartida(String codigo)` | — | 200, 404 |
| GET | `/api/partidas/{codigo}/estudiantes` | `obtenerEstudiantesDePartida(String codigo)` | — | 200, 404 |
| POST | `/api/partidas/{codigo}/estudiantes` | `agregarEstudianteAPartida(String codigo, Estudiante estudiante)` | JSON con `apodo` y `codigoPartida`; debe coincidir con `{codigo}` | 201, 400, 404, 409 |
| POST | `/api/partidas/{codigo}/respuestas` | `registrarRespuesta(String codigo, Respuesta respuesta)` | JSON de `Respuesta` | 200, 400, 404, 409 |

## Ejemplos de solicitudes y respuestas

### Crear primera pregunta de SQL

`POST http://localhost:8080/api/preguntas`

```json
{
  "enunciado": "¿Qué instrucción se usa para consultar datos en SQL?",
  "opciones": ["SELECT", "INSERT", "UPDATE", "DELETE"],
  "respuestaCorrecta": "SELECT",
  "explicacion": "SELECT permite consultar datos de una tabla.",
  "categoria": "SQL"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "enunciado": "¿Qué instrucción se usa para consultar datos en SQL?",
  "opciones": ["SELECT", "INSERT", "UPDATE", "DELETE"],
  "respuestaCorrecta": "SELECT",
  "explicacion": "SELECT permite consultar datos de una tabla.",
  "categoria": "SQL",
  "orden": 1
}
```

### Crear segunda pregunta de SQL

`POST http://localhost:8080/api/preguntas`

```json
{
  "enunciado": "¿Qué cláusula se usa para filtrar registros en SQL?",
  "opciones": ["WHERE", "ORDER BY", "GROUP BY", "VALUES"],
  "respuestaCorrecta": "WHERE",
  "explicacion": "WHERE permite filtrar los registros que cumplen una condición.",
  "categoria": "SQL"
}
```

Respuesta `201 Created`:

```json
{
  "id": 2,
  "enunciado": "¿Qué cláusula se usa para filtrar registros en SQL?",
  "opciones": ["WHERE", "ORDER BY", "GROUP BY", "VALUES"],
  "respuestaCorrecta": "WHERE",
  "explicacion": "WHERE permite filtrar los registros que cumplen una condición.",
  "categoria": "SQL",
  "orden": 2
}
```

### Crear partida usando las dos preguntas

Primero deben existir las preguntas con ids `1` y `2`. Luego se crea la partida:

`POST http://localhost:8080/api/partidas`

```json
{
  "codigo": "ABC123",
  "nombre": "Quiz básico de SQL",
  "idsPreguntas": [1, 2]
}
```

Respuesta `201 Created`:

```json
{
  "codigo": "ABC123",
  "nombre": "Quiz básico de SQL",
  "estado": "ESPERANDO",
  "idsPreguntas": [1, 2],
  "idsEstudiantes": []
}
```

### Ingresar estudiante a la partida

`POST http://localhost:8080/api/partidas/ABC123/estudiantes`

```json
{
  "apodo": "jpro24",
  "codigoPartida": "ABC123"
}
```

El campo `codigoPartida` es obligatorio y debe coincidir con el código incluido en la URL. Si falta o es diferente, la API responde `400 Bad Request`.

Respuesta `201 Created`:

```json
{
  "id": 1,
  "apodo": "jpro24",
  "codigoPartida": "ABC123",
  "puntaje": 0
}
```

### Iniciar la partida

`PUT http://localhost:8080/api/partidas/ABC123/estado`

```json
"EN_CURSO"
```

Respuesta `200 OK` con la partida en `estado: "EN_CURSO"`.

### Consultar las preguntas públicas de la partida

`GET http://localhost:8080/api/partidas/ABC123/preguntas`

Respuesta `200 OK`:

```json
[
  {
    "id": 1,
    "enunciado": "¿Qué instrucción se usa para consultar datos en SQL?",
    "opciones": ["SELECT", "INSERT", "UPDATE", "DELETE"],
    "categoria": "SQL",
    "orden": 1
  },
  {
    "id": 2,
    "enunciado": "¿Qué cláusula se usa para filtrar registros en SQL?",
    "opciones": ["WHERE", "ORDER BY", "GROUP BY", "VALUES"],
    "categoria": "SQL",
    "orden": 2
  }
]
```

Esta respuesta es un `PreguntaParaEstudiante`: no incluye `respuestaCorrecta` ni `explicacion`, de modo que el estudiante no recibe la solución antes de responder. La solución solo se entrega de forma indirecta en la retroalimentación del `ResultadoRespuesta` al registrar su respuesta.

### Registrar respuesta

`POST http://localhost:8080/api/partidas/ABC123/respuestas`

```json
{
  "estudianteId": 1,
  "preguntaId": 1,
  "opcionSeleccionada": "SELECT"
}
```

Respuesta `200 OK`:

```json
{
  "correcta": true,
  "puntosObtenidos": 100,
  "retroalimentacion": "Correcta. SELECT permite consultar datos de una tabla."
}
```

Si la opción es incorrecta: `correcta: false`, `puntosObtenidos: 0` y la retroalimentación comienza con "Incorrecta.".

### Respuesta duplicada

Si el mismo estudiante intenta responder de nuevo la misma pregunta:

`POST http://localhost:8080/api/partidas/ABC123/respuestas`

```json
{
  "estudianteId": 1,
  "preguntaId": 1,
  "opcionSeleccionada": "INSERT"
}
```

Respuesta `409 Conflict`:

```json
{
  "timestamp": "2026-09-06T15:10:00.000+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "El estudiante ya respondió esta pregunta en la partida ABC123."
}
```

La respuesta repetida no se registra y el puntaje del estudiante permanece igual (en el ejemplo, 100 puntos).

## Inyección por constructor

Los servicios y controladores no se instancian con `new` desde otros componentes: Spring los detecta por `@Service` y `@RestController` y los conecta mediante **inyección por constructor**.

```java
@RestController
@RequestMapping("/api")
public class PreguntaController {

    private final PreguntaService preguntaService;
    private final PartidaService partidaService;

    public PreguntaController(PreguntaService preguntaService, PartidaService partidaService) {
        this.preguntaService = preguntaService;
        this.partidaService = partidaService;
    }
}
```

Ventajas: el campo puede ser `final`, la dependencia es explícita, y las clases son fáciles de probar (la prueba del controlador inyecta un `PreguntaService` simulado con Mockito). `PartidaService` recibe `PreguntaService` y `EstudianteService` por el mismo mecanismo.

## Pruebas automatizadas

El proyecto cuenta con seis pruebas automatizadas que cubren las funcionalidades principales del avance. Están en `src/test/java/com/devkahoot/kahoot`:

| # | Prueba | Clase | Funcionalidad cubierta |
|---|---|---|---|
| 1 | `cuandoListarPreguntas_debeRetornarTodas` | `PreguntaServiceTest` | Listado completo de la lista real en memoria. |
| 2 | `cuandoAgregarPregunta_debeGuardarla` | `PreguntaServiceTest` | Alta de pregunta con id y orden asignados. |
| 3 | `cuandoActualizarPreguntaExistente_debeModificarla` | `PreguntaServiceTest` | Modificación conservando id y orden. |
| 4 | `cuandoEliminarPreguntaExistente_debeQuitarla` | `PreguntaServiceTest` | Borrado de una pregunta no utilizada. |
| 5 | `cuandoObtenerTodasLasPreguntas_desdeControlador_debeDelegarEnServicio` | `PreguntaControllerTest` | El controlador delega en el servicio simulado (Mockito), responde 200 y devuelve la lista esperada. |
| 6 | `cuandoResponderDosVecesLaMismaPregunta_debeRechazarLaSegundaRespuesta` | `PartidaServiceTest` | Regla de una sola respuesta: la primera respuesta suma puntos y la segunda genera `RespuestaDuplicadaException` sin volver a modificar el puntaje. |

Resultado esperado: seis pruebas ejecutadas, sin fallos ni errores.

### Criterio de diseño de las pruebas

Cada prueba se enfoca en una funcionalidad o regla de negocio principal. Puede contener varias aserciones siempre que todas comprueben el mismo comportamiento. Por ejemplo, `cuandoAgregarPregunta_debeGuardarla` verifica el objeto creado, su identificador y su orden porque los tres resultados pertenecen a la operación de alta.

La prueba de partida prepara una pregunta, una partida y un estudiante como condiciones necesarias para comprobar una sola regla: un estudiante no puede responder dos veces la misma pregunta. La validación de partidas inválidas y la consulta de preguntas públicas se documentan y se pueden demostrar mediante Postman, pero no se mezclan dentro de esta prueba.

Estas pruebas se presentan como test mediante el TDD.

### Demostración temporal de un fallo

Mostrar que una prueba detecta un resultado incorrecto modificando temporalmente una expectativa y luego restaurándola.

Ejemplo 1 — protección contra puntos duplicados:

En `PartidaServiceTest`, cambiar temporalmente:

```java
assertEquals(100, registrado.getPuntaje());
```

por:

```java
assertEquals(200, registrado.getPuntaje());
```

El test debe fallar porque el puntaje real permanece en `100`. Esto permite explicar que la segunda respuesta fue rechazada y no volvió a sumar puntos.

Ejemplo 2 — creación de una pregunta:

En `PreguntaServiceTest`, cambiar temporalmente:

```java
assertEquals(1, guardada.getId());
```

por:

```java
assertEquals(2, guardada.getId());
```

El test debe fallar porque la primera pregunta recibe el identificador `1`. Después de mostrar el fallo, se debe restaurar la expectativa original para que las seis pruebas vuelvan a pasar.

## Secuencia de demostración en Postman

Con la aplicación iniciada en `http://localhost:8080`:

1. `POST /api/preguntas` (x2) — crear las dos preguntas sencillas de SQL y confirmar que reciben los ids 1 y 2.
2. `GET /api/preguntas` — consultar el banco de preguntas.
3. `POST /api/partidas` — crear la partida con `"idsPreguntas": [1, 2]`; ambos ids deben existir.
4. `POST /api/partidas/ABC123/estudiantes` — enviar `apodo` y `codigoPartida: "ABC123"` para ingresar a la sala.
5. `GET /api/partidas/ABC123` y `GET /api/partidas/ABC123/estudiantes` — consultar la sala y sus participantes.
6. `PUT /api/partidas/ABC123/estado` con `"EN_CURSO"` — iniciar la partida.
7. `GET /api/partidas/ABC123/preguntas` — consultar las preguntas públicas (sin `respuestaCorrecta` ni `explicacion`).
8. `POST /api/partidas/ABC123/respuestas` — registrar una respuesta y recibir `ResultadoRespuesta`.
9. `POST /api/partidas/ABC123/respuestas` (de nuevo) — comprobar que la respuesta duplicada es rechazada con `409 Conflict` y el puntaje no cambia.

La colección completa está en `docs/DevKahoot_APF1.postman_collection.json`, ordenada según esta secuencia. No se elimina ninguna pregunta antes de crear la partida; los casos de actualización y eliminación van al final de la colección.

## Ejemplos de error

Para demostrar el manejo de errores en la defensa:

| Solicitud | Código | Motivo |
|---|---|---|
| `POST /api/preguntas` con `respuestaCorrecta: "xyz"` fuera de `opciones` | `400 Bad Request` | Pregunta inválida (la respuesta correcta debe estar entre las opciones). |
| `GET /api/preguntas/999` | `404 Not Found` | No existe la pregunta con id 999. |
| `POST /api/partidas` con `idsPreguntas: [1, 999]` | `400 Bad Request` | Todos los ids de preguntas deben existir antes de crear la partida. |
| `POST /api/partidas` dos veces con `codigo: "ABC123"` | `409 Conflict` | Código de partida duplicado. |
| `POST /api/partidas/ZZZ999/estudiantes` | `404 Not Found` | No existe la partida ZZZ999. |
| `POST /api/partidas/ABC123/estudiantes` sin `codigoPartida` o con código diferente | `400 Bad Request` | El estudiante debe enviar el código de sala y debe coincidir con la URL. |
| `POST /api/partidas/ABC123/estudiantes` con apodo repetido | `409 Conflict` | Ya existe un estudiante con ese apodo en la partida. |
| `DELETE /api/preguntas/1` cuando la pregunta pertenece a `ABC123` | `409 Conflict` | No se puede eliminar una pregunta usada por una partida. |
| `POST /api/partidas/ABC123/respuestas` con la partida en `ESPERANDO` | `404 Not Found` | Solo se aceptan respuestas cuando la partida está `EN_CURSO`. |
| `POST /api/partidas/ABC123/respuestas` repetida (mismo estudiante y pregunta) | `409 Conflict` | El estudiante ya respondió esa pregunta; no se registra la respuesta ni se suman puntos. |

