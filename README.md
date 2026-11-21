# Nexo Gamer

**Matias Muñoz** · Ingeniería en Desarrollo de Software · Duoc UC

**Seguridad y calidad en el desarrollo de software (ISY2202)** · Experiencia 2 · Semana 5

Nexo Gamer es un catálogo de eventos gamer con Spring Boot, Spring Security, Thymeleaf y MySQL. Esta versión incorpora publicación, carga de fotografías y videos, difusión, comentarios y valoraciones.

## Ejecutar localmente

Abre Docker Desktop. Desde la raíz del repositorio ejecuta en PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\iniciar.ps1
```

Abre `http://localhost:8090/eventos`. El inicializador genera claves aleatorias y levanta la aplicación junto a MySQL. Los cambios se guardan en un volumen de Docker.

| Cuenta de demostración local | Contraseña local | Rol |
| --- | --- | --- |
| `jugador@nexogamer.test` | `NexoDemo!27A` | Jugador |
| `invitado@nexogamer.test` | `NexoDemo!27B` | Jugador |
| `organizador@nexogamer.test` | `NexoDemo!27C` | Organizador |

En **Publicar**, el organizador crea un evento y carga medios desde su ficha. Puede difundirlo en **Comunidad** o abrir el enlace de WhatsApp o X. Los usuarios autenticados pueden publicar comentarios y mantener una valoración por evento.

## Capas y rutas

| Carpeta | Responsabilidad |
| --- | --- |
| `src/main/java/cl/nexogamer/seguridad` | Acceso, emisión de JWT, roles y reglas de Spring Security. |
| `src/main/java/cl/nexogamer/eventos` | Catálogo, publicación, participación y validación de archivos. |
| `src/main/resources/templates` y `static` | Vistas Thymeleaf, estilos y recursos. |
| `base-datos` | Esquema MySQL, datos de evaluación y tablas de participación. |
| `analisis-estatico` | Perfil de Semgrep y referencia inmutable de la imagen. |
| `pruebas` | Verificación del entorno local con Node.js. |

La portada, la búsqueda y el inicio de sesión son públicos. Las fichas, medios y funciones de participación requieren sesión web o JWT, según la ruta. `POST /api/login` devuelve un JWT con vigencia de 15 minutos. El permiso de organizador y la autoría se comprueban en las operaciones de gestión.

## Verificar y analizar

Con Docker Desktop y Node.js, después de iniciar la aplicación:

```powershell
node pruebas/verificar.mjs
powershell -NoProfile -File .\analisis-estatico\ejecutar.ps1 -Destino resultado-semgrep.json
```

La verificación crea un evento de integración y comprueba los permisos, la recuperación de una foto, la difusión, los comentarios y la actualización de la valoración. Semgrep revisa Java, Thymeleaf y CSS con el perfil versionado del proyecto.

## Desplegar en una VM con Docker

Con un dominio que apunte a la IP pública de la VM y los puertos 80 y 443 habilitados, prepara `.env` con claves propias y las variables de `.env.azure.ejemplo`. El directorio de datos de MySQL se conserva en un volumen dedicado.

```sh
docker compose -f compose.yaml -f compose.azure.yaml up -d --build base-datos aplicacion
bash infraestructura/renovar-accesos.sh
docker compose -f compose.yaml -f compose.azure.yaml up -d proxy
```

Distribuye los accesos de evaluación generados en `.accesos-evaluacion` por un canal privado. El proxy redirige `http://IP/eventos` al dominio HTTPS. Todavía se puede abrir la instancia local en el puerto 8090 desde la VM.

El origen y la licencia de las fotografías de referencia se indican en [CREDITOS.md](CREDITOS.md).
