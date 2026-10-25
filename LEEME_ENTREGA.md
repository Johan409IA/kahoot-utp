# Proyecto actualizado para revisión

Este paquete contiene el código fuente completo, frontend, pruebas, scripts de
arranque y documentación de AdaptCode UTP, actualizado el 23 de octubre de 2026.

## Por dónde empezar

1. Extraer la carpeta `adaptcode-utp` del ZIP en una ubicación local.
2. Leer [ENTREGA_2026-10-23.md](ENTREGA_2026-10-23.md): resume los cambios,
   las pruebas realizadas y los pendientes para continuar con otra IA.
3. Consultar [README.md](README.md), [el entorno local](docs/ENTORNO_LOCAL.md)
   y [el alcance del frontend](docs/FRONTEND.md).

Para arrancar en Windows, desde la carpeta extraída y con conexión a Internet:

```powershell
./scripts/start-local.ps1
```

El script descarga las herramientas que falten, prepara una base local propia
y ejecuta la aplicación en `http://127.0.0.1:8080/`.

## Qué se excluyó para reducir el tamaño

- `.tools/`: JDK, PostgreSQL, distribuciones y caché de dependencias de Maven.
- `target/`: clases compiladas y resultados de compilación/pruebas.
- `.local/`: base de desarrollo, credenciales cifradas, logs y archivos temporales.
- Configuración del editor, metadatos Git y archivos ZIP anteriores.

El paquete no transporta la base ni las cuentas de prueba del equipo original.
El primer arranque local genera nuevas credenciales y carga el catálogo inicial
del repositorio. No hace falta copiar herramientas o contraseñas de otro equipo.

## Antes de desplegar

La verificación realizada fue local con Java 25, Spring Boot y PostgreSQL.
El despliegue de producción no se ha realizado. La aplicación incluye su frontend
en `src/main/resources/static` y requiere el backend para usar sus funciones reales.

Revisar los requisitos y las variables de configuración del backend, y atender los
límites documentados de las salas en vivo. El script de Windows prepara desarrollo
local; no sustituye la configuración del servidor de despliegue.
