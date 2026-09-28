# API REST de catálogo — APF1

Base local: `http://localhost:8080`. El catálogo demo se habilita con el perfil Spring `demo` y requiere las variables de conexión a PostgreSQL indicadas en el README.

## Endpoints

### Consultar cursos

`GET /api/cursos`

```json
[
  { "id": 1, "nombre": "Algoritmos" },
  { "id": 2, "nombre": "Bases de Datos" },
  { "id": 3, "nombre": "Python" }
]
```

### Consultar temas de un curso

`GET /api/cursos/{cursoId}/temas`

Cada elemento contiene `id` y `nombre`. Solo se incluyen temas activos y se ordenan por nombre.

### Filtrar micro-retos

`GET /api/micro-retos?cursoId=1&temaId=1&nivel=BASICO`

`cursoId` es obligatorio; `temaId` y `nivel` son opcionales.

```json
[
  {
    "id": 1,
    "temaId": 1,
    "tipo": "SELECCION_MULTIPLE",
    "nivel": "BASICO",
    "enunciado": "Con edad = 18, ¿la condición edad >= 18 es verdadera?"
  }
]
```

### Consultar un micro-reto

`GET /api/micro-retos/{id}`

La propiedad `contenido` depende de `tipo`:

- `SELECCION_MULTIPLE`: `opciones` con `id` y `texto`.
- `ORDENAR_BLOQUES`: `bloques` con `id` y `texto` en el orden que ve el estudiante.
- `PREDECIR_SALIDA`: `codigo` con `lenguaje` y `codigo`, más `opciones`.
- `SELECCIONAR_FRAGMENTO`: `opciones` con `id`, `codigo` y `lenguaje`.

La respuesta omite la corrección, el orden esperado y la retroalimentación. Estos datos quedan para la etapa de evaluación de intentos.

## Códigos HTTP

| Estado | Caso |
|---|---|
| `200 OK` | Consulta correcta, incluso cuando una lista no tiene resultados (`[]`). |
| `400 Bad Request` | ID no positivo, nivel desconocido, falta `cursoId` o `temaId` no pertenece al curso. |
| `404 Not Found` | Curso, tema o micro-reto inexistente o inactivo. |
| `405 Method Not Allowed` | Se intenta usar un método distinto de `GET` en las rutas de consulta. |

No se define un manejador global de errores en esta etapa; se conservan las respuestas estándar de Spring.

## Datos de demostración

Con el perfil `demo`, la aplicación carga un tema de Condicionales y otro de Secuencias para Algoritmos, Expresiones para Python y Consultas SELECT para Bases de Datos. Incluye cuatro micro-retos básicos: evaluar `edad >= 18`, ordenar los pasos de una condición, predecir `print(2 + 3 * 2)` y elegir `SELECT * FROM estudiantes;`. El código de carga comprueba códigos estables y no duplica los registros al reiniciar.

## Verificación APF1

1. Ejecutar la aplicación desde IntelliJ con las variables `ADAPTCODE_DB_USERNAME` y `ADAPTCODE_DB_PASSWORD`, y el perfil `demo`.
2. Importar `AdaptCode_APF1.postman_collection.json` y probar las consultas positivas, listas vacías, filtros inválidos, recursos inexistentes y método no permitido.
3. Reiniciar una segunda vez y comprobar que no aparecen temas ni retos duplicados.
4. Revisar en PostgreSQL que las relaciones curso → tema → micro-reto → alternativas/bloques/retroalimentación estén completas.

Las pruebas `*ControllerTest` validan el contrato HTTP con MockMvc y servicios simulados. Las pruebas `*ServiceTest` validan filtros y reglas de lectura sin requerir la base PostgreSQL.
