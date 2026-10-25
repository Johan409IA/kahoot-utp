# Entorno local de AdaptCode UTP

## Iniciar el backend y PostgreSQL

Desde la raíz del proyecto en PowerShell:

```powershell
./scripts/start-local.ps1
```

El comando prepara los binarios si faltan y mantiene el backend en primer plano.
Abre [la aplicación real](http://127.0.0.1:8080/). No se utiliza el adaptador de demo
del navegador.

| Componente | Configuración verificada |
| --- | --- |
| Java | Eclipse Temurin 25.0.4.1, seleccionado por `mvnw.cmd` desde `.tools/java`. |
| Maven | 3.9.16, descargado por el wrapper; caché de distribución y dependencias en `.tools/m2`. |
| PostgreSQL | 18.6, binarios en `.tools/postgresql-dist/pgsql`, escucha solo en `127.0.0.1:5433`. |
| Base | `adaptcode_utp`, datos en `.local/postgres-data`. |
| Spring Boot | 4.1.1, escucha solo en `127.0.0.1:8080` con el perfil local. |

Las descargas provienen de la [API oficial de Temurin](https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse),
Maven Central y los [binarios de PostgreSQL publicados por EnterpriseDB](https://www.enterprisedb.com/download-postgresql-binaries).
El JDK se verifica con el SHA-256 publicado por Temurin antes de extraerlo.
No se reemplazó el Java global del equipo: el wrapper selecciona el JDK del proyecto.

Las credenciales de la base se generan aleatoriamente y se almacenan en
`.local/database-credentials.xml`, cifradas con Windows DPAPI para la cuenta que
las creó. El script las carga en variables del proceso del backend; no se imprimen
ni se incluyen en el código. `.tools/` y `.local/` se excluyen del control de versiones.
El archivo temporal de contraseña de `initdb` se elimina al finalizar su uso.

El primer arranque crea el catálogo institucional y, con los perfiles `local,demo`,
los cuatro micro-retos iniciales que ya incluía el repositorio. Son registros reales
en PostgreSQL con contenido de prueba local; no son datos de estudiantes de la UTP.
Las cuentas, intentos y preguntas creados durante la verificación también son sintéticos.

## Correcciones de arranque

- El wrapper consultaba `Target[0]` sobre una propiedad nula de directorios normales
  de Windows. Ahora trata correctamente tanto directorios como enlaces.
- `mvnw.cmd` utiliza el JDK local y fija un repositorio absoluto dentro del proyecto;
  ya no intenta crear `C:\.m2\repository`.
- El directorio temporal heredado por Java impedía crear el socket interno del
  selector NIO en este entorno de Windows. El arranque de desarrollo pasa
  `-Djdk.net.unixdomain.tmpdir=.local` al proceso de Spring Boot. Se comprobó con un
  selector Java y con Tomcat iniciado; no se cambiaron las protecciones de red.
- `ADAPTCODE_DB_URL` permite configurar la URL JDBC sin modificar el archivo de
  propiedades. Las variables de usuario y contraseña siguen siendo obligatorias
  fuera del arranque local.

## Detener y reiniciar

Detén primero Spring Boot con **Ctrl+C** en la terminal que ejecuta `start-local.ps1`.
PostgreSQL conserva todos los datos y puede seguir activo entre ejecuciones.
Para detener exclusivamente esa base, después de cerrar el backend:

```powershell
./scripts/start-local.ps1 -StopDatabase
```

Para iniciar solamente PostgreSQL:

```powershell
./scripts/start-local.ps1 -DatabaseOnly
```

No borres `.local/postgres-data` para reiniciar: vuelve a ejecutar el comando de
arranque. Si se pierde el archivo de credenciales de una base existente, el script
se detiene en lugar de reinicializarla.

## Verificar

```powershell
./mvnw.cmd -B -ntp test
npm run check:frontend
npm run test:frontend
npm run test:integration
```

Resultados comprobados el 23 de octubre de 2026:

- **5 pruebas de servicios Java aprobadas**, compilando los 168 archivos Java con release 25.
- **12 pruebas del frontend aprobadas** y sintaxis/imports de sus siete módulos correctos.
- **7 grupos de verificación integrada aprobados** contra Spring Boot y PostgreSQL:
  recursos públicos y 401; catálogo; registro/verificación/JWT de tres roles;
  cuatro tipos de evaluación, solución asistida, progreso e historial;
  preferencias; preguntas/alternativas/retroalimentación/quizzes; y salas/PIN/estados/ranking.
- Prueba desde la interfaz con cuenta de estudiante: error, explicación, acierto,
  punto e insignia; el progreso se mantuvo al recargar.
- Prueba desde la interfaz con cuenta de docente: creación y consulta de una
  pregunta persistida en PostgreSQL.
- Texto principal normal de **16 px** en 390 px y 1440 px de ancho; tamaño Grande
  de **18 px**, con zoom de página 1. Sin desbordamiento horizontal en esos tamaños.

`test:integration` solo admite direcciones de loopback, crea cuentas nuevas para
cada ejecución y guarda un resumen sin tokens ni contraseñas en
`.local/live-verification.json`. Los registros se conservan para poder revisarlos.

Capturas verificadas: [escritorio](screenshots/frontend-real.png) y
[móvil](screenshots/frontend-real-mobile.png).

Los límites de la partida en vivo descritos en [FRONTEND.md](FRONTEND.md) se mantienen:
la API actual todavía no expone preguntas y alternativas para una partida sincronizada
del estudiante. Se verificaron los endpoints existentes, sin simular los faltantes.
