# Frontend de AdaptCode UTP

## Ejecutar

El frontend se sirve desde `src/main/resources/static`. No necesita instalación de paquetes ni compilación JavaScript.

Para preparar Java 25, Maven y PostgreSQL locales en Windows y ejecutar la aplicación:

```powershell
./scripts/start-local.ps1
```

Abre `http://127.0.0.1:8080/`. Las solicitudes usan `/api` en el mismo origen y el token JWT de la cuenta. La configuración de seguridad permite únicamente el documento inicial, favicon y `/assets/**` como nuevos recursos públicos; conserva las reglas de la API. Consulta [el entorno local](ENTORNO_LOCAL.md) para instalación, credenciales y reinicio.

Para desarrollar o revisar el frontend sin levantar Spring Boot, con Node.js 22 o posterior:

```powershell
npm run dev
```

- Interfaz con API real: `http://127.0.0.1:4173/`.
- Demo explícita: `http://127.0.0.1:4173/?demo=1#/`.
- Acceso de la demo: `http://127.0.0.1:4173/?demo=1#/acceso`, con botones de estudiante y docente.
- PIN inicial de demostración: `482913`.

El servidor de desarrollo escucha únicamente en loopback y reenvía `/api` a `http://localhost:8080`. Puedes cambiar el puerto con `FRONTEND_PORT` y el destino de la API con `ADAPTCODE_API_ORIGIN`. No se necesita configurar CORS.

La demo guarda datos de ejemplo bajo `adaptcode.demo.data.v1` en el almacenamiento local y utiliza una sesión separada de la real. Solo se importa su adaptador con `?demo=1`. Una caída de la API real muestra un error y nunca cambia silenciosamente a datos ficticios. El código de verificación de la demo es `123456`; sus credenciales son simuladas y no representan cuentas del servidor.

## Arquitectura y alcance

Se revisaron los controladores, DTO, modelos, repositorios, servicios, seguridad, inicializadores, pruebas y documentación existentes. La aplicación utiliza Spring Boot MVC, JPA/PostgreSQL y JWT, y originalmente solo contenía el backend. La propuesta de Angular en el documento funcional se conserva como antecedente; esta implementación usa módulos ES y CSS para integrarse directamente en el servidor actual, sin introducir una cadena de compilación adicional.

| Archivo | Responsabilidad |
| --- | --- |
| `assets/app.js` | Navegación por hash, sesión, inicio, catálogo, filtros, auth, progreso, insignias, preferencias e ingreso a salas. |
| `assets/practice.js` | Cuatro tipos de reto, evaluación remota, retroalimentación, solución asistida, historial y desafío personal opcional. |
| `assets/teacher.js` | Preguntas, alternativas, explicaciones, quizzes ordenados, salas, participantes, estados, anulaciones y clasificación. |
| `assets/api.js` | Solicitudes JSON, JWT, tiempos de espera, errores por campo e invalidación por 401. |
| `assets/core.js` | Etiquetas, escape HTML, sesión, progreso y cuerpos de evaluación. |
| `assets/demo.js` | Adaptador aislado para demostración, sin solicitudes al backend. |
| `assets/styles.css` | Diseño responsive, contraste, texto ampliado y reducción de movimiento. |
| `scripts/preview.mjs` | Servidor local con proxy a la API y validación de rutas de archivos. |

La sesión real se conserva en `sessionStorage`, con expiración, y se elimina al cerrar sesión o recibir 401. No se almacenan contraseñas. Los identificadores de las rutas se obtienen de la sesión y los datos del servidor. Los textos provenientes de formularios y de la API se escapan antes de insertarse como HTML.

El catálogo público nunca se usa para calcular respuestas correctas. La práctica se califica mediante `POST /api/usuarios/{id}/intentos/evaluar`. Desde el tercer intento, si la API lo habilita, el estudiante puede elegir ver la solución; esa acción registra un intento con `acierto=true` y `usoAyuda=true` en el endpoint simple existente. El servidor conserva la regla de un único punto de dominio por reto.

Las vistas respetan el rol recibido en el login: estudiante UTP, docente UTP o usuario externo. Los formularios docentes muestran el rechazo del servidor cuando falta retroalimentación o se intenta modificar contenido protegido. El acceso real siempre depende de Spring Security y de las reglas del backend.

Las preferencias pueden guardarse antes del acceso en el navegador; después de iniciar sesión se cargan las de la cuenta, si existen. Incluye foco visible, salto al contenido, etiquetas, mensajes de estado, diálogos nativos y botones para ordenar bloques sin arrastrar. La señal sonora es opcional y tiene equivalente visual. La tipografía de Google Fonts es opcional y tiene fuentes locales de respaldo. No se ha realizado una auditoría formal WCAG.

## Límites de la API que se mantienen visibles

- **Salas:** el backend expone PIN, participantes, estado, respuestas declaradas y ranking, pero no una ruta para que el estudiante obtenga el quiz con sus alternativas ni un estado de pregunta actual. Por eso se implementan ingreso, consulta y control de sala, pero no se simula una partida sincronizada ni se piden aciertos al estudiante. Para completar la partida hacen falta endpoints de preguntas de sala y evaluación del servidor. No se modificó el modelo de juego en este trabajo.
- La actualización de salas y clasificación es manual con **Actualizar sala**. No existe integración WebSocket/SSE en el backend actual.
- El clonado institucional crea únicamente los metadatos en el servicio actual; la interfaz pide completar alternativas y explicaciones de la copia.
- El DTO de preguntas docentes no admite código base ni bloques. La creación conserva sus campos actuales; no añade un editor o ejecución de código que el servidor no soporte.
- El servidor otorga automáticamente `PRIMER_PASO` y `PERSEVERANTE`. Las otras tres insignias solo aparecen como obtenidas si la API las devuelve.
- No hay endpoint agregado de historial global o errores por revisar. El historial se consulta por micro-reto.
- Los códigos de verificación y recuperación se devuelven en las respuestas del backend actual. La interfaz los identifica como códigos recibidos de la API para esta versión de desarrollo y no afirma haber enviado correos.

## Validación

```powershell
npm run check:frontend
npm run test:frontend
```

Se verifican sintaxis e imports de los siete módulos. Las 12 pruebas cubren solicitudes con JWT, cuerpo de evaluación, orden de bloques, expiración, 401, entrada directa sin hash, filtros, validaciones, fallos de red, escape HTML y los flujos de progreso, solución y salas de la demo.

También se comprobaron en el navegador el acceso de estudiante y docente, el catálogo, la secuencia de errores y solución asistida, el progreso sin punto al usar ayuda y la creación de preguntas, alternativas, quizzes y salas. Se revisaron los tamaños de 390 px y 1440 px sin desbordamiento horizontal, el ordenamiento de bloques y la entrada directa con la API desconectada. Estas comprobaciones usan la demo; no sustituyen una prueba integrada con PostgreSQL y Spring Boot.

Se comprobó después la integración completa con PostgreSQL local: 5 pruebas Java y 7 grupos de comprobaciones HTTP aprobados. En el navegador se verificaron inicio de sesión, evaluación, progreso persistido tras recargar, preferencias y creación docente. Las cuentas y el catálogo inicial son de prueba, almacenados por el backend; no se usó el adaptador `?demo=1`.

El tamaño normal ahora parte de 16 px; párrafos, formularios y componentes usan unidades `rem`. Las preferencias Grande y Muy grande cambian la base a 18 y 20 px sin aplicar zoom al diseño. Se reajustaron tarjetas y paneles móviles para que el contenido siga cabiendo.

Capturas actuales con la API real: [escritorio](screenshots/frontend-real.png) y [móvil](screenshots/frontend-real-mobile.png). El [inicio de la demo anterior](screenshots/frontend-preview.png) se conserva como referencia histórica.
