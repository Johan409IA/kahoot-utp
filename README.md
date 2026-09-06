# DevKahoot

API REST desarrollada con **Spring Boot 4.1.1** y **Java 25** para el primer avance de proyecto final (APF1).

## Qué hace

- Un docente administra un banco de preguntas de selección múltiple (CRUD de `/api/preguntas`).
- El docente crea una partida con un código de sala y los ids de sus preguntas.
- Los estudiantes ingresan a la partida con el código de sala y un apodo.
- La partida pasa por los estados `ESPERANDO`, `EN_CURSO` y `FINALIZADA`.
- Mientras está `EN_CURSO`, cada estudiante responde cada pregunta una sola vez y recibe un resultado con puntos y retroalimentación (100 puntos por respuesta correcta).

## Características

- Arquitectura por capas: `model`, `service`, `controller`.
- Almacenamiento en memoria con `ArrayList` (sin base de datos ni JPA).
- Inyección de dependencias por constructor.
- Métodos y nombres de negocio en español.
- Las preguntas que consulta el estudiante (`GET /api/partidas/{codigo}/preguntas`) no incluyen `respuestaCorrecta` ni `explicacion`.
- Una respuesta duplicada del mismo estudiante a la misma pregunta responde `409 Conflict` y no modifica el puntaje.
- Los órdenes de las preguntas no se duplican al eliminar y crear preguntas.

## Documentación

- `docs/APF1_API_REST.md` — documentación completa de la API: modelos, endpoints, códigos HTTP, ejemplos JSON y secuencia de demostración.
- `docs/DevKahoot_APF1.postman_collection.json` — colección de Postman ordenada según la secuencia de demostración.

## Pruebas

El proyecto cuenta con seis pruebas automatizadas que cubren las funcionalidades principales del avance: CRUD de preguntas, delegación del controlador al servicio y el flujo principal de una partida con estudiante, incluyendo el rechazo de respuestas duplicadas.

## Fuera del alcance de este avance

Base de datos, JPA, capa `repository`, autenticación/seguridad, frontend y preguntas aleatorias.
