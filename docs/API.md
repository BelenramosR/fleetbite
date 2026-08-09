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

ISO 8601:

```text
2026-08-10T18:30:00Z
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

Formato sugerido:

```json
{
  "timestamp": "2026-08-10T18:30:00Z",
  "status": 409,
  "code": "INVALID_ORDER_TRANSITION",
  "message": "The order cannot transition from DELIVERED to PREPARING",
  "path": "/api/v1/orders/123/status",
  "correlationId": "f3c..."
}
```

---

## 4. Autenticación

```text
POST /auth/login
POST /auth/refresh
```

Ejemplo:

```json
{
  "email": "dispatcher@fleetbite.local",
  "password": "..."
}
```

Respuesta:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "expiresIn": 3600
}
```

Si no se implementa refresh token dentro del MVP, documentarlo claramente.

---

# 5. Orders

## GET /orders

Filtros:

```text
status
priority
driverId
from
to
page
size
sort
```

Ejemplo:

```text
GET /api/v1/orders?status=READY&priority=HIGH&page=0&size=20
```

---

## GET /orders/{id}

Devuelve detalle completo.

---

## POST /orders

```json
{
  "customerName": "Ana Torres",
  "customerPhone": "999999999",
  "deliveryAddress": "Av. Example 123",
  "deliveryLatitude": -12.1001,
  "deliveryLongitude": -77.0201,
  "totalAmount": 85.90,
  "promisedDeliveryAt": "2026-08-10T19:15:00Z"
}
```

---

## PUT /orders/{id}

Solo para campos permitidos por estado.

---

## DELETE /orders/{id}

Debe restringirse.

Por ejemplo:

```text
solo CREATED
```

Si el pedido inició el proceso operativo:

```text
409 Conflict
```

---

## PATCH /orders/{id}/status

```json
{
  "status": "READY"
}
```

La transición debe ser validada en backend.

---

## GET /orders/{id}/history

Devuelve eventos del pedido.

---

## GET /orders/{id}/assignments

Devuelve intentos de asignación.

---

# 6. Drivers

## GET /drivers

Filtros:

```text
status
vehicleType
page
size
```

---

## GET /drivers/{id}

---

## POST /drivers

```json
{
  "fullName": "Luis Gómez",
  "phone": "988000111",
  "vehicleId": "uuid"
}
```

---

## PUT /drivers/{id}

---

## PATCH /drivers/{id}/status

```json
{
  "status": "AVAILABLE"
}
```

---

## PATCH /drivers/{id}/location

```json
{
  "latitude": -12.102,
  "longitude": -77.028,
  "timestamp": "2026-08-10T18:20:00Z"
}
```

---

# 7. Vehicles

```text
GET    /vehicles
GET    /vehicles/{id}
POST   /vehicles
PUT    /vehicles/{id}
DELETE /vehicles/{id}
```

Ejemplo:

```json
{
  "plate": "1234-AB",
  "type": "MOTORCYCLE",
  "brand": "Honda",
  "model": "CB125"
}
```

---

# 8. Assignments

## POST /orders/{orderId}/assign

Asignación manual.

```json
{
  "driverId": "uuid"
}
```

---

## POST /orders/{orderId}/auto-assign

Puede existir para pruebas o modo manual.

En arquitectura event-driven, normalmente `ORDER_READY` disparará la asignación automáticamente.

---

## POST /assignments/{id}/accept

Solo conductor correspondiente.

---

## POST /assignments/{id}/reject

```json
{
  "reason": "Vehicle problem"
}
```

---

## POST /assignments/{id}/pickup

Marca pedido recogido.

---

## POST /assignments/{id}/complete

```json
{
  "deliveredAt": "2026-08-10T18:55:00Z"
}
```

---

## POST /assignments/{id}/fail

```json
{
  "reason": "CUSTOMER_NOT_AVAILABLE"
}
```

---

# 9. Dispatch

Endpoints orientados al panel operativo.

## GET /dispatch/board

Puede devolver:

```json
{
  "readyOrders": [],
  "waitingOrders": [],
  "activeDeliveries": [],
  "availableDrivers": [],
  "atRiskOrders": []
}
```

Debe usarse con cautela para no duplicar toda la API. Puede ser un read model optimizado para la vista.

---

## POST /dispatch/orders/{orderId}/reassign

```json
{
  "driverId": "uuid",
  "reason": "Previous driver unavailable"
}
```

---

# 10. Dashboard

## GET /dashboard/summary

Ejemplo:

```json
{
  "activeOrders": 18,
  "waitingForDriver": 3,
  "inTransit": 9,
  "atRisk": 2,
  "availableDrivers": 5,
  "averageAssignmentMinutes": 3.4,
  "averageDeliveryMinutes": 31.2,
  "onTimeRate": 0.94
}
```

---

# 11. Users

Solo Admin.

```text
GET    /users
GET    /users/{id}
POST   /users
PUT    /users/{id}
PATCH  /users/{id}/status
```

---

# 12. Paginación

Formato sugerido:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 84,
  "totalPages": 5
}
```

---

# 13. HTTP status

### 200

Consulta o actualización exitosa.

### 201

Recurso creado.

### 204

Operación sin body.

### 400

Validación.

### 401

No autenticado.

### 403

No autorizado.

### 404

No encontrado.

### 409

Conflicto de dominio.

Ejemplos:

```text
driver already assigned
invalid order transition
order already delivered
optimistic locking conflict
```

### 422

Puede utilizarse para ciertas reglas funcionales, aunque se recomienda mantener una política consistente.

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
promisedDeliveryAt > createdAt
```

El frontend puede validar por UX.

El backend siempre debe validar nuevamente.

---

# 15. Seguridad por endpoint

Ejemplo:

```text
/orders/**
ADMIN
RESTAURANT_OPERATOR
DISPATCHER

/dispatch/**
ADMIN
DISPATCHER

/drivers/{id}/location
DRIVER correspondiente

/assignments/{id}/accept
DRIVER correspondiente

/users/**
ADMIN
```

---

# 16. Versionado

Se recomienda:

```text
/api/v1
```

No es necesario implementar múltiples versiones dentro del examen.

---

# 17. OpenAPI

La implementación debe exponer Swagger.

Objetivos:

- probar endpoints;
- visualizar schemas;
- documentar seguridad;
- facilitar revisión técnica.

Cada endpoint importante debe tener:

- summary;
- response codes;
- request schema;
- response schema.

---

# 18. Correlation ID

El backend puede aceptar:

```text
X-Correlation-Id
```

Si no existe, genera uno.

Respuesta:

```text
X-Correlation-Id: uuid
```

Esto ayuda a correlacionar logs y eventos.

---

# 19. Idempotency Key

Para operaciones donde duplicados sean peligrosos puede admitirse:

```text
Idempotency-Key
```

Especialmente útil en creación de pedidos desde integraciones externas.

No es imprescindible para el MVP.

---

# 20. Endpoint de salud

```text
GET /actuator/health
```

No debe requerir autenticación si solo expone estado básico.

---

# 21. Contrato mínimo para el examen

Endpoints imprescindibles:

```text
POST /auth/login

GET  /orders
POST /orders
GET  /orders/{id}
PUT  /orders/{id}
DELETE /orders/{id}
PATCH /orders/{id}/status

GET  /drivers
POST /drivers
PUT  /drivers/{id}
PATCH /drivers/{id}/status

GET  /vehicles
POST /vehicles
PUT  /vehicles/{id}

POST /orders/{id}/assign
GET  /orders/{id}/history

GET /dashboard/summary
```

El resto puede añadirse progresivamente.

---

# 22. Resumen

La API debe reflejar dos tipos de operaciones:

### CRUD

```text
orders
drivers
vehicles
users
```

### Comandos de negocio

```text
ready
assign
accept
reject
pickup
complete
reassign
```

Esta diferencia es importante.

No todos los procesos de negocio deberían disfrazarse como un `PUT` genérico sobre una entidad.
