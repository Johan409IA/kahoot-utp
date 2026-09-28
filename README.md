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

El esquema se actualiza automáticamente durante el desarrollo local mediante `spring.jpa.hibernate.ddl-auto=update`; no se debe usar ese ajuste como estrategia de migración en producción.

## Ejecutar

```powershell
./mvnw.cmd spring-boot:run
```

Con el perfil `demo` habilitado, la aplicación crea cursos si faltan y carga temas y cuatro micro-retos de demostración sin duplicarlos al reiniciar.

## API REST

- `GET /api/cursos`
- `GET /api/cursos/{cursoId}/temas`
- `GET /api/micro-retos?cursoId={id}&temaId={id}&nivel=BASICO`
- `GET /api/micro-retos/{id}`

Los filtros `temaId` y `nivel` son opcionales. El nivel admite `BASICO`, `INTERMEDIO` y `AVANZADO`. Una búsqueda sin resultados responde `200` con `[]`; los identificadores inexistentes responden `404`; filtros inválidos responden `400`. En esta etapa solo se ofrecen operaciones `GET`.

Los ejemplos de respuesta, escenarios y códigos HTTP están en [`docs/AdaptCode_APF1_API_REST.md`](docs/AdaptCode_APF1_API_REST.md). La colección importable de Postman está en [`docs/AdaptCode_APF1.postman_collection.json`](docs/AdaptCode_APF1.postman_collection.json).

## Pruebas

```powershell
./mvnw.cmd test
```
