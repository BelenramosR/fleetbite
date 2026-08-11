# Despliegue de FleetBite: Vercel + Cloud Run + Neon

## Estado

Despliegue público completado y verificado el 10 de agosto de 2026.

| Componente | Plataforma | URL / configuración |
|---|---|---|
| Frontend React | Vercel | `https://fleetbite.vercel.app` |
| Backend Spring Boot | Google Cloud Run | `https://fleetbite-backend-119563175788.us-central1.run.app` |
| API REST | Cloud Run | `/api/v1` |
| Swagger / OpenAPI | Cloud Run | `/swagger-ui/index.html` / `/v3/api-docs` |
| PostgreSQL 17 | Neon | Región AWS US East 2; conexión SSL mediante secretos |

## Arquitectura desplegada

```text
Usuario
  -> HTTPS -> Vercel (React)
  -> HTTPS + Authorization Bearer -> Cloud Run (Spring Boot)
  -> JDBC SSL -> Neon (PostgreSQL 17)
```

El backend usa facturación por solicitud, mínimo cero y máximo una instancia para reducir
el consumo de la demostración. La imagen está almacenada en Artifact Registry, región
`us-central1`.

## Seguridad aplicada

- No existen credenciales de nube o base de datos versionadas en Git.
- Cloud Run obtiene URL, usuario, contraseña y secreto JWT desde Secret Manager.
- La cuenta de ejecución solo puede leer los cuatro secretos concretos de FleetBite.
- CORS permite únicamente `https://fleetbite.vercel.app`.
- PostgreSQL exige SSL.
- El secreto JWT no tiene fallback local público.
- `/auth/login` tiene limitación de intentos configurable.
- Swagger se mantiene habilitado para la exposición técnica.

La autenticación actual usa access y refresh tokens en `localStorage`, enviados mediante
`Authorization: Bearer`. No usa cookies. Migrar a cookies `HttpOnly`, `Secure` y `SameSite`
requiere un cambio coordinado en frontend y backend y queda fuera del camino crítico del
despliegue de presentación.

## Variables públicas

Vercel conserva para producción:

```text
VITE_API_BASE_URL=https://fleetbite-backend-119563175788.us-central1.run.app/api/v1
```

Cloud Run conserva, entre otras, esta configuración:

```text
FRONTEND_ALLOWED_ORIGIN=https://fleetbite.vercel.app
SPRING_PROFILES_ACTIVE=docker
SWAGGER_ENABLED=true
```

Los valores sensibles se inyectan desde Secret Manager y no se documentan aquí.

## Base de datos y migraciones

Flyway creó el esquema de Neon desde cero y lo dejó en la versión `v11`. La migración V11
corrige el borrado de pedidos con historial mediante `ON DELETE CASCADE` sobre
`order_history.order_id`.

## Verificaciones realizadas

- Build remoto del contenedor: correcto.
- Revisión activa de Cloud Run: saludable.
- `GET /actuator/health`: HTTP 200.
- `GET /v3/api-docs`: HTTP 200.
- Frontend público de Vercel: HTTP 200.
- Preflight CORS desde el origen estable de Vercel: HTTP 200 y origen permitido correcto.
- Conexión Hikari a Neon: correcta.
- Flyway: 11 migraciones aplicadas sin errores.

## Próximos pasos

1. Ejecutar un smoke test visual desde el enlace público con cada rol.
2. Confirmar login, creación de pedido, asignación y flujo completo del driver.
3. Conectar el proyecto de Vercel al repositorio Git si se desean despliegues automáticos.
4. Mantener `min-instances=0` y `max-instances=1` durante la demostración.
5. Tras la exposición, rotar secretos si alguno fue compartido fuera de los gestores.
