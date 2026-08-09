# FleetBite - Contrato de API REST

## 1. Propósito

Este documento define una propuesta de endpoints REST para FleetBite.

Swagger/OpenAPI será la fuente ejecutable del contrato, mientras que este documento explica la organización conceptual.

Base path propuesta:

```text
/api/v1
```

---

## 2. Convenciones

### Formato

```text
application/json
```

### Fechas

ISO 8601 con offset de negocio `-05:00` (no UTC `Z`):

```text
2026-08-10T18:30:00-05:00
```

### IDs

Pueden ser UUID internamente.

Los códigos visibles pueden ser:

```text
ORD-2026-0001
DRV-0001
```

---

## 3. Respuesta de error

Formato real (`ApiErrorResponse`):

```json
{
  "timestamp": "2026-08-10T23:30:00Z",
  "status": 409,
  "code": "INVALID_ORDER_TRANSITION",
  "message": "The order cannot transition from DELIVERED to PREPARING",
  "path": "/api/v1/orders/11111111-1111-1111-1111-111111111111/confirm"
}
```

Campos: `timestamp`, `status`, `code`, `message`, `path`. No hay `correlationId`.

---

## 4. Autenticación

```text
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

Públicos (sin JWT). No hay self-register: las cuentas se crean con `POST /api/v1/users` (ADMIN).

Login:

```json
{
  "email": "dispatcher@fleetbite.local",
  "password": "Fleetbite1!"
}
```

Respuesta:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Refresh (rota el refresh token):

```json
{ "refreshToken": "..." }
```

Logout revoca el refresh (204, idempotente). Usar `Authorization: Bearer <accessToken>` en el resto de endpoints.

---

# 5. Orders

Roles: ADMIN, RESTAURANT_OPERATOR, DISPATCHER.

```text
GET    /api/v1/orders
POST   /api/v1/orders
GET    /api/v1/orders/{id}
PUT    /api/v1/orders/{id}
DELETE /api/v1/orders/{id}
```

No hay filtros/paginación en el listado actual.

## POST /orders

`promisedDeliveryAt` lo calcula el backend; no se envía en el request.

```json
{
  "customerName": "Ana Torres",
  "customerPhone": "999999999",
  "deliveryAddress": "Av. Example 123",
  "deliveryLatitude": -12.1001,
  "deliveryLongitude": -77.0201,
  "totalAmount": 85.90
}
```

## PUT / DELETE

Solo mientras el pedido está en `CREATED` (si no, 409).

## Workflow (comandos explícitos)

No existe `PATCH /orders/{id}/status`. Transiciones vía:

```text
POST /api/v1/orders/{id}/confirm
POST /api/v1/orders/{id}/start-preparation
POST /api/v1/orders/{id}/ready
POST /api/v1/orders/{id}/cancel
```

`ready` dispara auto-assign **después del commit** vía evento local `ORDER_READY`
(consistencia eventual: la respuesta HTTP de `/ready` refleja TX A y puede mostrar `READY`
aunque luego el pedido quede `ASSIGNED` o `WAITING_FOR_DRIVER`).

Fallback manual: `POST /api/v1/orders/{id}/auto-assign`.

## GET /orders/{id}/history

Historial append-only real (`order_history`).

---

# 6. Drivers

Roles: ADMIN, DISPATCHER.

Relación: `User (role=DRIVER) 1 — 0..1 Driver 0..1 — 0..1 Vehicle`.

```text
GET    /api/v1/drivers
POST   /api/v1/drivers
GET    /api/v1/drivers/{id}
PUT    /api/v1/drivers/{id}
DELETE /api/v1/drivers/{id}
PATCH  /api/v1/drivers/{id}/location
POST   /api/v1/drivers/{id}/online
POST   /api/v1/drivers/{id}/offline
PUT    /api/v1/drivers/{id}/vehicle
DELETE /api/v1/drivers/{id}/vehicle
```

Estados driver: `OFFLINE`, `AVAILABLE`, `BUSY`.

Flujo: primero `POST /users` con `role=DRIVER`, luego `POST /drivers` con ese `userId`.

## POST /drivers

```json
{
  "userId": "uuid",
  "phone": "988000111",
  "currentLatitude": -12.102,
  "currentLongitude": -77.028
}
```

`name` en la response viene de `User.fullName` (no se almacena en Driver).

## PUT /drivers/{id}/vehicle

```json
{ "vehicleId": "uuid" }
```

Asigna vehículo (`AVAILABLE` → `IN_USE`). `DELETE .../vehicle` desasigna (`IN_USE` → `AVAILABLE`).

## PATCH /drivers/{id}/location

```json
{
  "latitude": -12.102,
  "longitude": -77.028
}
```

Disponibilidad vía `online` / `offline`. Delete driver exige `OFFLINE` y sin vehículo.

---

# 7. Vehicles

Roles: ADMIN, DISPATCHER.

```text
GET    /api/v1/vehicles
POST   /api/v1/vehicles
GET    /api/v1/vehicles/{id}
PUT    /api/v1/vehicles/{id}
DELETE /api/v1/vehicles/{id}
POST   /api/v1/vehicles/{id}/maintenance
POST   /api/v1/vehicles/{id}/activate
POST   /api/v1/vehicles/{id}/deactivate
```

Estados: `AVAILABLE`, `IN_USE`, `MAINTENANCE`, `INACTIVE`.

```json
{
  "plate": "ABC-123",
  "type": "MOTORCYCLE"
}
```

Nuevo vehículo nace `AVAILABLE`. No eliminar un vehículo asignado a un Driver (409).
---

# 8. Assignments

Asignación (entry points bajo Orders; ADMIN/DISPATCHER):

```text
POST /api/v1/orders/{orderId}/assign
POST /api/v1/orders/{orderId}/auto-assign
```

Ciclo de vida (ADMIN/DISPATCHER):

```text
GET  /api/v1/assignments
GET  /api/v1/assignments/{id}
POST /api/v1/assignments/{id}/accept
POST /api/v1/assignments/{id}/reject
POST /api/v1/assignments/{id}/pickup
POST /api/v1/assignments/{id}/start-delivery
POST /api/v1/assignments/{id}/complete
```

Estados de assignment: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED`.

## POST /orders/{orderId}/assign

```json
{
  "driverId": "uuid"
}
```

## POST /orders/{orderId}/auto-assign

- Candidatos: drivers `AVAILABLE` con ubicación **y** vehículo asignado.
- Distancia Haversine (km); menor distancia gana.
- `assignmentScore` actualmente equivale a `distanceKm`.
- Sin driver: HTTP 200, `assigned=false`, `reason=NO_AVAILABLE_DRIVER`; order → `WAITING_FOR_DRIVER`.
- No se dispara automáticamente desde `ready`.

## POST /assignments/{id}/reject

```json
{
  "reason": "Vehicle problem"
}
```

`complete` no recibe body. No existe endpoint `fail` en la implementación actual.

---

# 9. Dispatch

No implementado en el core actual. El panel operativo usa los endpoints de orders/drivers/assignments.

---

# 10. Dashboard

No implementado (`GET /dashboard/summary` no existe aún).

---

# 11. Users

Solo ADMIN.

```text
GET  /api/v1/users
POST /api/v1/users
GET  /api/v1/users/{id}
PUT  /api/v1/users/{id}
POST /api/v1/users/{id}/activate
POST /api/v1/users/{id}/deactivate
```

No hay `PATCH /users/{id}/status`.

---

# 12. Paginación

No implementada en los listados actuales (devuelven arrays completos).

---

# 13. HTTP status

### 200

Consulta o actualización exitosa (incluye auto-assign con `assigned=false`).

### 201

Recurso creado.

### 204

Operación sin body.

### 400

Validación.

### 401

No autenticado (`AUTHENTICATION_FAILED`).

### 403

No autorizado (`ACCESS_DENIED` / `USER_INACTIVE` en login).

### 404

No encontrado.

### 409

Conflicto de dominio (transición inválida, duplicados, locking, etc.).

### 500

Error inesperado.

---

# 14. Validaciones

Ejemplos:

```text
customerName required
totalAmount >= 0
valid coordinates
valid phone
```

`promisedDeliveryAt` lo calcula el backend.

El frontend puede validar por UX. El backend siempre valida nuevamente.

---

# 15. Seguridad por endpoint

Implementado con `requestMatchers` (no `@PreAuthorize`):

```text
POST /api/v1/auth/login                         permitAll
GET  /actuator/health                           permitAll
/swagger-ui/**, /v3/api-docs/**                 permitAll

/api/v1/users/**                                ADMIN
POST /api/v1/orders/*/assign|auto-assign        ADMIN, DISPATCHER
/api/v1/orders/**                               ADMIN, RESTAURANT_OPERATOR, DISPATCHER
/api/v1/drivers/**                              ADMIN, DISPATCHER
/api/v1/vehicles/**                             ADMIN, DISPATCHER
/api/v1/assignments/**                          ADMIN, DISPATCHER
```

---

# 16. Versionado

```text
/api/v1
```

---

# 17. OpenAPI

Expuesto con springdoc-openapi:

```text
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

Security scheme global: `bearerAuth` (JWT). Login está excluido explícitamente.

Tags: Authentication, Users, Orders, Drivers, Vehicles, Assignments.

Health actuator queda fuera del documento OpenAPI.

---

# 18. Correlation ID

No implementado en el contrato de error actual.

---

# 19. Idempotency Key

No implementado en el MVP.

---

# 20. Endpoint de salud

```text
GET /actuator/health
```

Público. No documentado en OpenAPI.

---

# 21. Contrato mínimo implementado

```text
POST /api/v1/auth/login

GET/POST/PUT/DELETE /api/v1/orders
POST /api/v1/orders/{id}/confirm|start-preparation|ready|cancel
GET  /api/v1/orders/{id}/history
POST /api/v1/orders/{id}/assign
POST /api/v1/orders/{id}/auto-assign

GET/POST/PUT/DELETE /api/v1/drivers
PATCH /api/v1/drivers/{id}/location
POST /api/v1/drivers/{id}/online|offline

GET/POST/PUT/DELETE /api/v1/vehicles
POST /api/v1/vehicles/{id}/maintenance|activate|deactivate

GET /api/v1/assignments
POST /api/v1/assignments/{id}/accept|reject|pickup|start-delivery|complete

GET/POST/PUT /api/v1/users
POST /api/v1/users/{id}/activate|deactivate
```

---

# 22. Resumen

La API distingue CRUD y comandos de negocio (`confirm`, `ready`, `assign`, `accept`, `pickup`, `complete`, etc.).

No todos los procesos de negocio se disfrazan como un `PUT`/`PATCH` genérico sobre una entidad.
