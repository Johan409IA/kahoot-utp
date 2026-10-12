# API REST - AdaptCode UTP

Documentación de referencia de la API REST de AdaptCode UTP.

Base local: `http://localhost:8080`.

La colección Postman con el flujo de exposición (lista para importar y
ejecutar en orden) está en
[`AdaptCode_APF2.postman_collection.json`](AdaptCode_APF2.postman_collection.json).

## Guía de exposición 

Flujo verificado en vivo con un docente (`c20240001@utp.edu.pe`) y un
estudiante (`u20240001@utp.edu.pe`), tres preguntas SQL de Bases de Datos
(tema Consultas SELECT, `id 4`), quiz de 3 ítems y sala en vivo. Los IDs y
tokens se encadenan solos por variables de la colección; solo hay que
ejecutar de arriba abajo.

> Nota: si el registro responde `400` (`El correo ya está registrado`) es
> porque ese correo ya existe de una prueba anterior: se salta al login.
> Los IDs mostrados (`preguntas 2, 3, 4`, `quiz 6`, `sala 2`, PIN
> `437661`) son los de la ejecución de referencia; en una BD fresca los
> IDs los asigna la colección sola.

### 1. Auth (6 requests)

1. **Registro docente** `POST /api/auth/registro` — crea al docente y
   devuelve `usuarioId` y el `codigoVerificacion` de 6 dígitos.
   ```json
   { "nombres": "Carlos", "apellidos": "Mendoza",
     "correo": "c20240001@utp.edu.pe", "contrasena": "Clave1234" }
   ```
2. **Verificar docente** `POST /api/auth/verificar-cuenta` — con el
   código del paso 1.
   ```json
   { "correo": "c20240001@utp.edu.pe", "codigo": "{{codigoDocente}}" }
   ```
3. **Login docente** `POST /api/auth/login` — guarda `tokenDocente`
   (`DOCENTE_UTP`).
   ```json
   { "correo": "c20240001@utp.edu.pe", "contrasena": "Clave1234" }
   ```
4. **Registro estudiante** `POST /api/auth/registro`.
   ```json
   { "nombres": "Ana", "apellidos": "Paredes",
     "correo": "u20240001@utp.edu.pe", "contrasena": "Clave1234" }
   ```
5. **Verificar estudiante** `POST /api/auth/verificar-cuenta`.
   ```json
   { "correo": "u20240001@utp.edu.pe", "codigo": "{{codigoEstudiante}}" }
   ```
6. **Login estudiante** `POST /api/auth/login` — guarda
   `tokenEstudiante` (`ESTUDIANTE_UTP`).
   ```json
   { "correo": "u20240001@utp.edu.pe", "contrasena": "Clave1234" }
   ```

### 2. Catálogo público (4 requests, sin token)

7. **Listar cursos** `GET /api/cursos` → Algoritmos (`1`), Bases de
   Datos (`3`), Python (`2`).
8. **Listar temas** `GET /api/cursos/3/temas` → Consultas SELECT
   (`id 4`).
9. **Filtrar micro-retos** `GET
   /api/micro-retos?cursoId=3&temaId=4&nivel=BASICO`.
10. **Detalle del micro-reto** `GET /api/micro-retos/4` — muestra las
    opciones SQL sin revelar la correcta.

### 3. Docente: 3 preguntas, 6 alternativas, 3 feedbacks, quiz y sala

11. **Crear pregunta 1** `POST /api/docentes/{{docenteId}}/preguntas`.
    ```json
    { "temaId": 4, "tipo": "SELECCIONAR_FRAGMENTO", "nivel": "BASICO",
      "enunciado": "¿Qué consulta obtiene todas las filas de la tabla estudiantes?" }
    ```
12. **Alternativa P1 correcta** `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId1}}/alternativas`.
    ```json
    { "texto": null, "codigo": "SELECT * FROM estudiantes;",
      "lenguaje": "sql", "ordenVisual": 1, "correcta": true }
    ```
13. **Alternativa P1 incorrecta** `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId1}}/alternativas`.
    ```json
    { "texto": null, "codigo": "SELECT estudiantes FROM *;",
      "lenguaje": "sql", "ordenVisual": 2, "correcta": false }
    ```
14. **Feedback P1 (Q19)** `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId1}}/retroalimentaciones` — solo en la
    incorrecta, una por alternativa.
    ```json
    { "alternativaId": "{{alt1IncorrectaId}}",
      "causa": "El nombre de la tabla se coloco en la lista de columnas.",
      "explicacion": "En SELECT se indican las columnas antes de FROM y el nombre de la tabla despues.",
      "secuenciaTextual": "Escribir SELECT y las columnas; agregar FROM y luego la tabla.",
      "sugerencia": "Identifica que parte nombra las columnas y cual nombra la tabla." }
    ```
15. **Crear pregunta 2** `POST /api/docentes/{{docenteId}}/preguntas`.
    ```json
    { "temaId": 4, "tipo": "SELECCIONAR_FRAGMENTO", "nivel": "BASICO",
      "enunciado": "¿Qué consulta lista el nombre y la edad de todos los estudiantes?" }
    ```
16. **Alternativa P2 correcta** `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId2}}/alternativas`.
    ```json
    { "texto": null, "codigo": "SELECT nombre, edad FROM estudiantes;",
      "lenguaje": "sql", "ordenVisual": 1, "correcta": true }
    ```
17. **Alternativa P2 incorrecta** (falta la coma) `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId2}}/alternativas`.
    ```json
    { "texto": null, "codigo": "SELECT nombre edad FROM estudiantes;",
      "lenguaje": "sql", "ordenVisual": 2, "correcta": false }
    ```
18. **Feedback P2 (Q19)** `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId2}}/retroalimentaciones`.
    ```json
    { "alternativaId": "{{alt2IncorrectaId}}",
      "causa": "Falta la coma entre columnas: SQL lo lee como un solo identificador.",
      "explicacion": "En SELECT las columnas se separan con comas antes de FROM.",
      "secuenciaTextual": "Escribir SELECT; listar nombre, coma, edad; agregar FROM estudiantes.",
      "sugerencia": "Revisa cuantas columnas pide el enunciado y separalas con comas." }
    ```
19. **Crear pregunta 3** `POST /api/docentes/{{docenteId}}/preguntas`.
    ```json
    { "temaId": 4, "tipo": "SELECCIONAR_FRAGMENTO", "nivel": "BASICO",
      "enunciado": "¿Qué consulta lista el nombre de los estudiantes mayores de edad?" }
    ```
20. **Alternativa P3 correcta** `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId3}}/alternativas`.
    ```json
    { "texto": null, "codigo": "SELECT nombre FROM estudiantes WHERE edad >= 18;",
      "lenguaje": "sql", "ordenVisual": 1, "correcta": true }
    ```
21. **Alternativa P3 incorrecta** (operador invertido `=>`) `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId3}}/alternativas`.
    ```json
    { "texto": null, "codigo": "SELECT nombre FROM estudiantes WHERE edad => 18;",
      "lenguaje": "sql", "ordenVisual": 2, "correcta": false }
    ```
22. **Feedback P3 (Q19)** `POST /api/docentes/{{docenteId}}/preguntas/{{preguntaId3}}/retroalimentaciones`.
    ```json
    { "alternativaId": "{{alt3IncorrectaId}}",
      "causa": "El operador esta invertido: en SQL se escribe mayor o igual como >=.",
      "explicacion": ">= incluye la igualdad; => no es un operador valido en SQL.",
      "secuenciaTextual": "Escribir WHERE edad; agregar >=; comparar con 18.",
      "sugerencia": "Distingue > de >= y recuerda el orden de los simbolos." }
    ```
23. **Crear quiz (3 preguntas)** `POST /api/docentes/{{docenteId}}/quizzes`.
    ```json
    { "titulo": "SELECT basico - repaso 3 preguntas",
      "items": [
        { "preguntaId": "{{preguntaId1}}", "orden": 1 },
        { "preguntaId": "{{preguntaId2}}", "orden": 2 },
        { "preguntaId": "{{preguntaId3}}", "orden": 3 } ] }
    ```
24. **Marcar quiz Listo** `POST /api/docentes/{{docenteId}}/quizzes/{{quizId}}/listo` — exige
    ≥1 ítem y feedback completo en cada incorrecta (Q19); si falta,
    responde `400` (mostrar ese rechazo es parte de la demo).
25. **Crear sala** `POST /api/docentes/{{docenteId}}/salas` — responde
    con el PIN de 6 dígitos (se guarda solo en `{{pin}}`).
    ```json
    { "quizId": "{{quizId}}", "tiempoPorPregunta": 60,
      "ampliacion": "ESTANDAR" }
    ```

### 4. Sala en vivo (9 requests)

26. **Abrir espera** `POST /api/docentes/{{docenteId}}/salas/{{salaId}}/abrir-espera`
    (docente) — sin este paso nadie puede unirse.
27. **Buscar por PIN** `GET /api/salas/por-pin?pin={{pin}}`
    (estudiante).
28. **Unirse** `POST /api/salas/{{salaId}}/participaciones`
    (estudiante).
    ```json
    { "estudianteId": "{{estudianteId}}" }
    ```
29. **Iniciar** `POST /api/docentes/{{docenteId}}/salas/{{salaId}}/iniciar` (docente) — la
    sala pasa a `EN_CURSO`.
30. **Responder P1** `POST /api/salas/{{salaId}}/respuestas`
    (estudiante, un envío único por pregunta).
    ```json
    { "participacionId": "{{participacionId}}",
      "preguntaId": "{{preguntaId1}}",
      "acierto": true, "omitida": false, "tiempoMs": 8000 }
    ```
31. **Responder P2** `POST /api/salas/{{salaId}}/respuestas`.
    ```json
    { "participacionId": "{{participacionId}}",
      "preguntaId": "{{preguntaId2}}",
      "acierto": true, "omitida": false, "tiempoMs": 7000 }
    ```
32. **Responder P3** (fallo a propósito, para ver el ranking con 2/3) `POST /api/salas/{{salaId}}/respuestas`.
    ```json
    { "participacionId": "{{participacionId}}",
      "preguntaId": "{{preguntaId3}}",
      "acierto": false, "omitida": false, "tiempoMs": 9000 }
    ```
33. **Ranking** `GET /api/salas/{{salaId}}/ranking` — en la ejecución
    de referencia: `posicion: 1, puntos: 2, tiempoMs: 24000`.
34. **Finalizar** `POST /api/docentes/{{docenteId}}/salas/{{salaId}}/finalizar` (docente).

### 5. Práctica autónoma (3 requests)

35. **Evaluar fallo** `POST /api/usuarios/{{estudianteId}}/intentos/evaluar` (repetir 3 veces
    con la misma incorrecta del reto 4: `SELECT estudiantes FROM *;`,
    `alternativaId 7` en BD fresca). Los 2 primeros devuelven la
    explicación **sin revelar la correcta** (`solucion: null`); al
    3.er intento, `verSolucionDisponible: true` con la solución (Q47).
    ```json
    { "microRetoId": 4, "alternativaId": 7, "ordenBloques": null,
      "usoAyuda": false, "tiempoMs": 4000 }
    ```
36. **Progreso** `GET /api/usuarios/{{estudianteId}}/progreso` — en
    la referencia: `completado: true, puntoDominio: true`.
37. **Insignias** `GET /api/usuarios/{{estudianteId}}/insignias` —
    `PRIMER_PASO` + `PERSEVERANTE`.

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
| `POST /{id}/abrir-espera` | `BORRADOR` → `ESPERA`; habilita el ingreso — `200`. |
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
2. Importar `AdaptCode_APF2.postman_collection.json` y ejecutar la
   “Guía de exposición” en orden (los 37 requests del flujo SQL de 3
   preguntas).
3. Reiniciar y comprobar que no hay duplicados.
4. `./mvnw.cmd test`: 5 pruebas de servicio.