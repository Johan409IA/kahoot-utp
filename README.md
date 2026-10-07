# AdaptCode UTP API

API REST de la plataforma educativa AdaptCode UTP, construida con Spring Boot 4.1.1, Java 25 y PostgreSQL.

## Alcance implementado

- Consulta de cursos, temas y micro-retos para práctica autónoma.
- Los micro-retos cubren selección múltiple, ordenamiento de bloques, predicción de salida y selección de fragmentos.
- Las respuestas HTTP no exponen la alternativa correcta, el orden correcto ni la retroalimentación de evaluación.
- El rol base de usuario se representa con `ESTUDIANTE_UTP`, `DOCENTE_UTP` y `USUARIO_EXTERNO`.
- Los datos de demostración se cargan únicamente con el perfil `demo`.

## Requisitos locales

- Java 25.
- PostgreSQL disponible en `localhost:5433`.
- Base de datos `adaptcode_utp`.
- Variables de entorno `ADAPTCODE_DB_USERNAME` y `ADAPTCODE_DB_PASSWORD`.

En IntelliJ, configura estas dos variables en la configuración `AdaptCodeApplication`. Para cargar temas y micro-retos de demostración, agrega `demo` a **Active profiles** o define `SPRING_PROFILES_ACTIVE=demo`. El perfil no cambia la URL de la API.

