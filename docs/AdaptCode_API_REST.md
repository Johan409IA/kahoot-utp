# API REST - AdaptCode UTP

Documentación de referencia de la API REST de AdaptCode UTP.

Base local: `http://localhost:8080`.

La colección Postman con el flujo de exposición (lista para importar y
ejecutar en orden) está en
[`AdaptCode_APF2.postman_collection.json`](AdaptCode_APF2.postman_collection.json).

## Autenticación y roles

La API usa JWT. Tras `POST /api/auth/login` se recibe `token`; en los
endpoints protegidos se envía:

```http
Authorization: Bearer <token>
```

| Rol | Acceso |
|---|---|
| `ESTUDIANTE_UTP` (`U+dígitos@utp.edu.pe`) | Práctica autónoma, salas en vivo, progreso, insignias, preferencias. |
| `DOCENTE_UTP` (`C+dígitos@utp.edu.pe`) | Preguntas y quizzes propios, salas propias y su analítica. |
| `USUARIO_EXTERNO` (correo convencional) | Solo práctica autónoma. No entra a salas. |

Público (sin token): `POST /api/auth/**` y `GET` de catálogo
(`/api/cursos/**`, `/api/micro-retos/**`). El resto exige token; cada rol
solo accede a lo suyo (401 sin token, 403 con rol insuficiente).

## 1. Autenticación (`/api/auth`)

### Registrar cuenta

`POST /api/auth/registro` — `201`. El rol deriva del correo: `U+dígitos`
estudiante, `C+dígitos` docente, otro formato externo. Contraseña 8-72
caracteres. Devuelve el código de verificación de 6 dígitos (10 minutos).

```json
{ "nombres": "Ana", "apellidos": "Paredes",
  "correo": "u20240001@utp.edu.pe", "contrasena": "Clave1234" }
```

### Iniciar sesión

`POST /api/auth/login` — `200`. Credenciales inválidas o cuenta sin
verificar responden `401` con mensaje (sin revelar si el correo existe).

```json
{ "correo": "u20240001@utp.edu.pe", "contrasena": "Clave1234" }
```

```json
{ "token": "eyJhbGciOi...", "tipoToken": "Bearer", "expiraEnMs": 3600000,
  "usuarioId": 1, "correo": "u20240001@utp.edu.pe",
  "nombres": "Ana", "apellidos": "Paredes", "rol": "ESTUDIANTE_UTP" }
```

### Verificar cuenta

`POST /api/auth/verificar-cuenta` — `200`.

```json
{ "correo": "u20240001@utp.edu.pe", "codigo": "482913" }
```

### Reenviar código / recuperar / restablecer

`POST /api/auth/reenviar-codigo` — `200`, siempre neutral:
`Si el correo está registrado, recibirás un código.`

```json
{ "correo": "u20240001@utp.edu.pe" }
```

`POST /api/auth/recuperar-contrasena` — `200`, mismo mensaje neutral.

`POST /api/auth/restablecer-contrasena` — `200`. Cambiar la clave invalida
los tokens anteriores (hay que volver a iniciar sesión).

```json
{ "correo": "u20240001@utp.edu.pe", "codigo": "482913",
  "nuevaContrasena": "Clave5678" }
```

## 2. Catálogo público

### Listar cursos

`GET /api/cursos` — `200`.

```json
[ { "id": 1, "nombre": "Algoritmos" } ]
```

### Listar temas por curso

`GET /api/cursos/{cursoId}/temas` — `200` (`[]` si no hay). Curso
inexistente o inactivo: `404`.

### Filtrar micro-retos

`GET /api/micro-retos?cursoId=1&temaId=1&nivel=BASICO` — `200`.
`cursoId` obligatorio; `temaId` y `nivel` (`BASICO`, `INTERMEDIO`,
`AVANZADO`) opcionales.

### Detalle del micro-reto

`GET /api/micro-retos/{id}` — `200`. El `contenido` depende de `tipo`
(`opciones`, `bloques`, `codigo`+`opciones`, fragmentos). Nunca expone la
correcta, el orden esperado ni la retroalimentación.

## 3. Preguntas del docente (rol docente)

Base: `/api/docentes/{docenteId}/preguntas`.

| Método y ruta | Acción |
|---|---|
| `POST /` | Crear pregunta propia — `201`. Body: `temaId`, `tipo`, `nivel`, `enunciado`. |
| `POST /{microRetoId}/clonar` | Clonar micro-reto institucional como copia privada — `201`. |
| `GET /?origen=PROPIA` | Listar propias (`origen` opcional: `PROPIA`, `COPIA`, `INSTITUCIONAL`). |
| `GET /{id}` | Mostrar una — `200`. |
| `PUT /{id}` | Actualizar copia/propia — `200`. Institucional o usada en quiz `LISTO`: `400`. |
| `DELETE /{id}` | Borrado lógico (`activa=false`) — `200`. |

### Alternativas (`.../preguntas/{preguntaId}/alternativas`)

`POST` — `201`. `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.

```json
{ "texto": "Verdadero", "codigo": null, "lenguaje": null,
  "ordenVisual": 1, "correcta": true }
```

### Retroalimentaciones (`.../preguntas/{preguntaId}/retroalimentaciones`)

`POST` — `201`. `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
Solo en alternativas incorrectas, una por alternativa.

```json
{ "alternativaId": 2, "causa": "Confunde el operador.",
  "explicacion": ">= incluye la igualdad.",
  "secuenciaTextual": "Comparar 18 con 18; verificar >=; obtener verdadero.",
  "sugerencia": "Distingue > de >=." }
```

## 4. Quizzes (rol docente)

Base: `/api/docentes/{docenteId}/quizzes`.

| Método y ruta | Acción |
|---|---|
| `POST /` | Crear en `BORRADOR` con ítems — `201`. |
| `GET /?estado=` | Listar (`estado` opcional: `BORRADOR`, `LISTO`). |
| `GET /{id}` | Mostrar con ítems — `200`. |
| `PUT /{id}` | Reemplazar título e ítems (solo `BORRADOR`) — `200`. |
| `DELETE /{id}` | Eliminar (solo `BORRADOR`) — `200`. |
| `POST /{id}/listo` | Pasar a `LISTO` — `200`. Exige ≥1 ítem y retroalimentación completa en cada incorrecta (Q19); si falta, `400`. Un quiz `LISTO` ya no se edita ni elimina. |

```json
{ "titulo": "Condicionales - repaso",
  "items": [ { "preguntaId": 10, "orden": 1 } ] }
```

## 5. Salas (rol docente, salvo indicación)

Base: `/api/docentes/{docenteId}/salas`.

| Método y ruta | Acción |
|---|---|
| `POST /` | Crear en `BORRADOR` con PIN de 6 dígitos generado — `201`. Body: `quizId` (debe estar `LISTO`), `tiempoPorPregunta` (20, 30, 60, 90 o 120), `ampliacion` (`ESTANDAR`, `MAS_50`, `MAS_100`). |
| `GET /?estado=` | Listar propias (`estado` opcional). |
| `GET /{id}` | Mostrar — `200`. |
| `POST /{id}/iniciar` | `BORRADOR`/`ESPERA` → `EN_CURSO`; congela la versión del quiz — `200`. |
| `POST /{id}/pausar` | `EN_CURSO` → `PAUSADA` — `200` (no acepta respuestas en pausa). |
| `POST /{id}/reanudar` | `PAUSADA` → `EN_CURSO` — `200`. |
| `POST /{id}/finalizar` | `EN_CURSO`/`PAUSADA` → `FINALIZADA` — `200`. |

### Buscar sala por PIN (rol estudiante)

`GET /api/salas/por-pin?pin=482913` — `200`. PIN inválido: `400`;
inexistente: `404`.

## 6. Sala en vivo

### Participaciones (`/api/salas/{salaId}/participaciones`)

| Método y ruta | Acción y rol |
|---|---|
| `POST /` | Unirse (estudiante): body `estudianteId`. Solo en `SALA_ESPERA`, un lugar por cuenta — `201`. |
| `GET /` | Listar (docente y estudiante) — `200`. |
| `GET /{id}` | Mostrar — `200`. |
| `POST /{id}/expulsar?docenteId=` | Expulsar, docente, solo pre-inicio y reversible — `200`. |
| `POST /{id}/readmitir?docenteId=` | Readmitir — `200`. |

### Respuestas (`/api/salas/{salaId}/respuestas`)

| Método y ruta | Acción |
|---|---|
| `POST /` | Registrar (estudiante, sala `EN_CURSO`): `participacionId`, `preguntaId`, `acierto`, `omitida`, `tiempoMs`. Un envío único por pregunta; `omitida` fuerza `acierto=false`; anuladas se rechazan — `201`. |
| `GET /?participacionId=` | Listar por participación (docente y estudiante). |
| `GET /{id}` | Mostrar. |
| `GET /por-pregunta?participacionId=&preguntaId=` | Envío propio a una pregunta (§6.2). |

### Anulaciones (rol docente)

`POST /api/docentes/{docenteId}/salas/{salaId}/anulaciones` — `201`.
Body: `preguntaId`, `motivo`. Una vez por pregunta, nunca en sala
`FINALIZADA` (sin efecto estadístico). `GET /` las lista.

### Ranking (docente y estudiante)

`GET /api/salas/{salaId}/ranking` — `200`. Un punto por acierto, tiempo
acumulado como desempate, empate total con salto (`1, 1, 3`); anuladas
excluidas. Vista estudiante (nombre + inicial del apellido).

```json
[ { "posicion": 1, "participacionId": 3, "estudianteId": 7,
    "nombre": "Ana", "apellidoInicial": "P.",
    "puntos": 2, "tiempoMs": 9000 } ]
```

## 7. Práctica autónoma (autenticado)

### Registrar intento

`POST /api/usuarios/{usuarioId}/intentos` — `201`. El `acierto` lo declara
el cliente solo en este endpoint simple (para calificación del servidor
usar `/evaluar`).

```json
{ "microRetoId": 1, "acierto": true, "usoAyuda": false, "tiempoMs": 5000 }
```

`GET /?microRetoId=` historial por reto; `GET /{id}` detalle. Sin
`PUT`/`DELETE`: los intentos se conservan.

### Evaluar con retroalimentación

`POST /api/usuarios/{usuarioId}/intentos/evaluar` — `201`. Califica en el
servidor, registra intento + progreso + insignias y devuelve la explicación
**sin revelar la correcta**. Tras el 3.er intento incluye la solución
explicada (usarla = `usoAyuda`, sin punto).

```json
{ "microRetoId": 1, "alternativaId": 5, "ordenBloques": null,
  "usoAyuda": false, "tiempoMs": 4000 }
```

Para `ORDENAR_BLOQUES`: `alternativaId` nulo y `ordenBloques` con los ids
en el orden propuesto.

### Progreso, insignias y preferencias

| Método y ruta | Acción |
|---|---|
| `GET /api/usuarios/{usuarioId}/progreso` | Progresos del usuario. |
| `GET /api/usuarios/{usuarioId}/progreso/{microRetoId}` | Progreso en un reto (`completado`, `resueltoConAyuda`, `puntoDominio`). |
| `GET /api/usuarios/{usuarioId}/insignias` | Insignias obtenidas (una sola vez, sin ventajas). |
| `GET /api/usuarios/{usuarioId}/preferencias` | Preferencias de accesibilidad — `404` si no hay. |
| `PUT /api/usuarios/{usuarioId}/preferencias` | Crear/actualizar (upsert). Sin `DELETE`. |

```json
{ "tema": "NORMAL", "tamanoTexto": "NORMAL",
  "reduccionMovimiento": false, "sonidoActivado": true,
  "modoDaltonismo": false }
```

## Códigos HTTP

| Estado | Caso |
|---|---|
| `200 OK` | Consulta o actualización correcta; listas vacías con `[]`. |
| `201 Created` | Creación correcta. |
| `400 Bad Request` | Validación (`campo -> mensaje`), regla de negocio (`{error:...}`), PIN/nivel/tiempo inválidos. |
| `401 Unauthorized` | Sin token, token inválido/expirado o credenciales incorrectas. |
| `403 Forbidden` | Rol insuficiente (p. ej. externo en salas, estudiante en rutas docentes). |
| `404 Not Found` | Recurso inexistente, inactivo o ajeno (misma respuesta para no enumerar). |
| `405 Method Not Allowed` | Método no permitido en la ruta. |
| `500` | Error interno. |

## Datos de demostración

Con el perfil `demo`, la aplicación carga los cursos Algoritmos, Python y
Bases de Datos con temas y cuatro micro-retos básicos (condición
`edad >= 18`, ordenar pasos, `print(2 + 3 * 2)`, `SELECT * FROM
estudiantes;`). La carga es idempotente: comprueba códigos estables y no
duplica al reiniciar.

## Verificación

1. Ejecutar con `ADAPTCODE_DB_USERNAME`, `ADAPTCODE_DB_PASSWORD` y perfil `demo`.
2. Importar `AdaptCode_APF2.postman_collection.json` y ejecutar las carpetas
   en orden: Auth → Catálogo → Docente → Sala en vivo → Práctica autónoma.
3. Reiniciar y comprobar que no hay duplicados.
4. `./mvnw.cmd test`: 5 pruebas de servicio (quiz+Q19, intento+punto, evaluación, ranking).
