# FleetBite

Sistema web para gestionar pedidos, asignaciones de reparto, conductores y vehículos.

## Stack

- Java 21 y Spring Boot
- PostgreSQL 17 y Flyway
- React, TypeScript y Vite
- Docker Compose
- Spring Security con JWT
- MapStruct y Lombok
- JUnit, ArchUnit, jMolecules y JaCoCo

## Arquitectura

El backend utiliza arquitectura hexagonal y está dividido en los módulos funcionales
`order`, `delivery`, `driver`, `vehicle` e `identity`.

```text
HTTP / REST                 PostgreSQL
     |                          ^
     v                          |
Inbound adapters -> Application ports/use cases -> Outbound ports
                         |
                         v
                       Domain
```

Cada módulo separa:

- `domain`: reglas, agregados y value objects sin dependencias de Spring;
- `application`: casos de uso y puertos de entrada/salida;
- `infrastructure`: controladores REST, seguridad, configuración y persistencia JPA.

Se eligió esta arquitectura porque mantiene la lógica de negocio independiente del framework
y de PostgreSQL, reduce el acoplamiento entre módulos y facilita probar los casos de uso sin
levantar toda la aplicación. ArchUnit y jMolecules verifican automáticamente que `domain` y
`application` no dependan de `infrastructure`.

## Requisitos

- Docker Desktop
- Node.js 22
- Corepack/pnpm 10
- JDK 21, solo si se ejecutará el backend fuera de Docker

## Ejecución local recomendada

### 1. Preparar variables

Desde la raíz del repositorio:

```powershell
Copy-Item .env.example .env
```

Edita `.env` y configura al menos:

```env
POSTGRES_PASSWORD=una-clave-local
SPRING_DATASOURCE_PASSWORD=una-clave-local
JWT_SECRET=un-secreto-aleatorio-de-al-menos-32-caracteres
```

`POSTGRES_PASSWORD` y `SPRING_DATASOURCE_PASSWORD` deben tener el mismo valor. Puedes generar
un secreto JWT con:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

El backend no tiene un secreto JWT de respaldo: si `JWT_SECRET` falta o es demasiado corto,
la aplicación no inicia. El archivo `.env` no debe subirse a Git.

### 2. Levantar backend y PostgreSQL

```powershell
docker compose up --build -d
docker compose ps
```

Comprueba que el backend esté disponible:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Resultado esperado: estado `UP`.

### 3. Levantar el frontend

En otra terminal:

```powershell
cd frontend
corepack enable
pnpm install
pnpm dev
```

Abre:

```text
http://localhost:8443
```

Vite redirige `/api/**` a `http://localhost:8080`, por lo que no hace falta modificar el
frontend para conectarlo con el backend local.

## URLs locales

| Recurso | URL |
|---|---|
| Aplicación web | http://localhost:8443 |
| Backend | http://localhost:8080 |
| API base | http://localhost:8080/api/v1 |
| Health | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| PostgreSQL | `127.0.0.1:5432` |

Swagger es la referencia ejecutable para consultar contratos, request bodies, respuestas y
códigos HTTP de todos los endpoints.

## Usuarios de demostración

Contraseña común: `Fleetbite1!`

| Correo | Rol |
|---|---|
| `admin@fleetbite.local` | `ADMIN` |
| `dispatcher@fleetbite.local` | `DISPATCHER` |
| `operator@fleetbite.local` | `RESTAURANT_OPERATOR` |
| `driver@fleetbite.local` | `DRIVER` |

## Autenticación de endpoints

Obtén los tokens mediante:

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@fleetbite.local",
  "password": "Fleetbite1!"
}
```

Para invocar un endpoint protegido incluye:

```http
Authorization: Bearer <accessToken>
```

El access token dura una hora por defecto. El refresh token dura siete días, se almacena
hasheado en PostgreSQL y rota cuando se llama a `/api/v1/auth/refresh`. El logout revoca el
refresh token mediante `/api/v1/auth/logout`.

## Endpoints principales

Todos parten de `http://localhost:8080/api/v1`.

### Autenticación

| Método | Ruta | Función | Acceso |
|---|---|---|---|
| POST | `/auth/login` | Iniciar sesión | Público |
| POST | `/auth/refresh` | Renovar y rotar tokens | Público con refresh token |
| POST | `/auth/logout` | Revocar refresh token | Público con refresh token |

### Pedidos

| Método | Ruta | Función |
|---|---|---|
| GET / POST | `/orders` | Listar o crear pedidos |
| GET / PUT / DELETE | `/orders/{id}` | Consultar, actualizar o eliminar |
| POST | `/orders/{id}/confirm` | Confirmar pedido |
| POST | `/orders/{id}/start-preparation` | Iniciar preparación |
| POST | `/orders/{id}/ready` | Marcar listo para recoger |
| POST | `/orders/{id}/cancel` | Cancelar pedido |
| GET | `/orders/{id}/history` | Consultar historial |
| POST | `/orders/{id}/assign` | Asignar manualmente |
| POST | `/orders/{id}/auto-assign` | Asignar al conductor disponible más cercano al local |

Los pedidos pueden ser operados por `ADMIN`, `RESTAURANT_OPERATOR` y `DISPATCHER`. Las
asignaciones manual y automática corresponden a `ADMIN` y `DISPATCHER`.

### Flujo propio del driver

| Método | Ruta | Función |
|---|---|---|
| GET | `/drivers/me` | Consultar el perfil autenticado |
| PATCH | `/drivers/me/location` | Actualizar ubicación propia |
| POST | `/drivers/me/online` | Quedar disponible |
| POST | `/drivers/me/offline` | Quedar fuera de línea |
| GET | `/driver/assignments/active` | Consultar asignación activa |
| GET | `/driver/assignments/summary` | Consultar resumen del driver |
| POST | `/driver/assignments/{id}/accept` | Aceptar asignación propia |
| POST | `/driver/assignments/{id}/reject` | Rechazar asignación propia |
| POST | `/driver/assignments/{id}/pickup` | Confirmar recojo |
| POST | `/driver/assignments/{id}/start-delivery` | Iniciar traslado al cliente |
| POST | `/driver/assignments/{id}/complete` | Confirmar entrega |

Estas rutas exigen rol `DRIVER`. El backend obtiene el usuario desde el JWT y bloquea el
acceso o modificación de asignaciones pertenecientes a otro conductor.

### Administración y operación

| Recurso | Rutas base | Roles principales |
|---|---|---|
| Usuarios | `/users` | `ADMIN` |
| Conductores | `/drivers` | `ADMIN`, `DISPATCHER` |
| Vehículos | `/vehicles` | `ADMIN`, `DISPATCHER` |
| Asignaciones operativas | `/assignments` | `ADMIN`, `DISPATCHER` |

Los recursos incluyen operaciones CRUD y acciones de estado. Revisa Swagger para conocer los
cuerpos exactos y todas las variantes.

## Respuesta HTTP estándar

La API utiliza un envelope consistente:

```json
{
  "code": "SUCCESS",
  "success": true,
  "data": {},
  "errors": []
}
```

Los errores de validación, dominio, autenticación, autorización, conflicto y recursos no
encontrados se transforman centralmente mediante `@RestControllerAdvice`.

## Pruebas

Backend completo:

```powershell
cd backend
.\mvnw.cmd clean verify
```

Incluye pruebas unitarias, integración con PostgreSQL, reglas arquitectónicas y cobertura
JaCoCo.

Frontend:

```powershell
cd frontend
pnpm build
```

## Comandos útiles

```powershell
docker compose logs -f backend
docker compose logs -f postgres
docker compose down
```

`docker compose down` conserva la base de datos. Para borrar todos los datos locales y hacer
que Flyway reconstruya el esquema:

```powershell
docker compose down -v
docker compose up --build -d
```

## Problemas frecuentes

- Backend no inicia: verifica `JWT_SECRET` y que las dos contraseñas PostgreSQL coincidan.
- Puerto ocupado: comprueba los puertos `5432`, `8080` y `8443`.
- Datos antiguos: ejecuta `docker compose down -v` únicamente si aceptas borrar la BD local.
- Frontend sin API: confirma que el health del backend responda antes de abrir el frontend.
- Respuesta `401`: inicia sesión y envía el access token como Bearer.
- Respuesta `403`: el usuario está autenticado, pero su rol o identidad no permite la acción.
