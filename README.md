# AdaptCode UTP API

API REST de la plataforma educativa AdaptCode UTP, construida con Spring Boot
4.1.1, Java 25, Spring Security + JWT y PostgreSQL.

## Alcance implementado

- **Auth con JWT** (`/api/auth`): registro por patrón de correo (`U` estudiante,
  `C` docente, resto externo), login, verificación con código de 6 dígitos,
  recuperación y restablecimiento con invalidación de sesiones.
- **Catálogo público**: cursos, temas y micro-retos (selección múltiple,
  ordenamiento de bloques, predicción de salida y selección de fragmentos).
  Sin token. No expone la correcta, el orden esperado ni la retroalimentación.
- **Docente** (rol `DOCENTE_UTP`): preguntas propias y copias, alternativas,
  retroalimentaciones (Q19), quizzes `BORRADOR`/`LISTO`, salas con PIN de 6
  dígitos y máquina de estados (`BORRADOR` → `ESPERA` → `EN_CURSO` ⇄
  `PAUSADA` → `FINALIZADA`), participaciones, anulaciones y ranking
  (`1, 1, 3`, tiempo como desempate).
- **Práctica autónoma** (autenticado): evaluación en servidor con
  retroalimentación causal sin revelar la correcta, solución explicada desde
  el 3.er intento, intentos ilimitados, progreso con punto de dominio único,
  insignias, preferencias de accesibilidad e historial.
- Los datos de demostración se cargan únicamente con el perfil `demo`.

## Requisitos locales

- Java 25.
- PostgreSQL disponible en `localhost:5433`.
- Base de datos `adaptcode_utp`.
- Variables de entorno `ADAPTCODE_DB_USERNAME` y `ADAPTCODE_DB_PASSWORD`.

En IntelliJ, configura estas dos variables en la configuración
`AdaptCodeApplication` y agrega `demo` a **Active profiles** (o define
`SPRING_PROFILES_ACTIVE=demo`) para cargar temas y micro-retos de
demostración. El perfil no cambia la URL de la API (`http://localhost:8080`).

## Ejecutar

```powershell
./mvnw.cmd spring-boot:run
```

## Probar

```powershell
./mvnw.cmd test
```

5 pruebas de servicio (quiz + Q19, intento + punto, evaluación, ranking).
La colección Postman del flujo de exposición (auth → catálogo → docente →
sala → autónoma, con variables auto-encadenadas) está en
[`docs/AdaptCode_APF2.postman_collection.json`](docs/AdaptCode_APF2.postman_collection.json)
y la referencia completa de endpoints en
[`docs/AdaptCode_API_REST.md`](docs/AdaptCode_API_REST.md).
