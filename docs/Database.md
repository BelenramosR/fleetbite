# FleetBite - Modelo de Datos y Persistencia

## 1. Propósito

Este documento define la estrategia de persistencia de FleetBite utilizando PostgreSQL.

El modelo debe soportar:

- pedidos;
- repartidores;
- vehículos;
- asignaciones;
- historial;
- usuarios y roles;
- eventos;
- SLA;
- trazabilidad;
- concurrencia.

La base de datos forma parte del núcleo transaccional del sistema.

---

## 2. Tecnología

Se propone:

```text
PostgreSQL
Spring Data JPA
Hibernate
Flyway
```

Motivos:

- soporte sólido de transacciones;
- restricciones;
- índices;
- tipos de datos adecuados;
- buen soporte en Docker;
- integración madura con Spring Boot.

---

## 3. Principio de modelado

El modelo debe representar el proceso operativo, no solamente pantallas.

Relaciones principales:

```text
User
 |
Driver
 |
Vehicle

Order
 |
DeliveryAssignment
 |
Driver

Order
 |
OrderHistory

Order
 |
OutboxEvent
```

---

## 4. Tabla orders

```text
orders
-------------------------
id
code
customer_name
customer_phone
delivery_address
delivery_latitude
delivery_longitude
total_amount
status
priority
created_at
confirmed_at
prepared_at
ready_at
assigned_at
picked_up_at
delivered_at
promised_delivery_at
version
```

### Reglas

- `code` debe ser único;
- `status` obligatorio;
- `priority` obligatorio;
- coordenadas pueden ser obligatorias si la asignación por distancia está habilitada;
- `version` se utiliza para optimistic locking.

---

## 5. Tabla drivers

```text
drivers
-------------------------
id
user_id
full_name
phone
status
current_latitude
current_longitude
last_location_at
created_at
updated_at
version
```

Estados:

```text
AVAILABLE
ASSIGNED
DELIVERING
OFFLINE
SUSPENDED
```

Reglas:

- un repartidor suspendido no puede ser asignado;
- un repartidor offline no debe considerarse disponible.

---

## 6. Tabla vehicles

```text
vehicles
-------------------------
id
plate
type
brand
model
status
created_at
updated_at
```

Tipos:

```text
MOTORCYCLE
BICYCLE
CAR
```

Estados:

```text
AVAILABLE
IN_USE
MAINTENANCE
INACTIVE
```

---

## 7. Relación driver_vehicle

Dependiendo del alcance, puede modelarse con FK directa o historial.

Versión simple:

```text
drivers.vehicle_id
```

Versión con trazabilidad:

```text
driver_vehicle_assignments
-------------------------
id
driver_id
vehicle_id
started_at
ended_at
```

Para el examen, la versión simple suele ser suficiente.

---

## 8. Tabla delivery_assignments

Es una entidad clave.

```text
delivery_assignments
-------------------------
id
order_id
driver_id
vehicle_id
status
assigned_at
accepted_at
rejected_at
picked_up_at
completed_at
rejection_reason
assignment_score
created_by
version
```

Estados:

```text
PENDING
ACCEPTED
REJECTED
CANCELLED
COMPLETED
```

No se recomienda almacenar únicamente `driver_id` dentro de `orders`, porque se perdería el historial de reasignaciones.

---

## 9. Historial de pedidos

```text
order_history
-------------------------
id
order_id
previous_status
new_status
event_type
description
performed_by
created_at
metadata_json
```

Ejemplos:

```text
PREPARING -> READY
READY -> ASSIGNED
ASSIGNED -> PICKED_UP
IN_TRANSIT -> DELIVERED
```

El historial debe ser append-only en el flujo normal.

---

## 10. Usuarios

```text
users
-------------------------
id
email
password_hash
full_name
status
created_at
updated_at
```

Roles:

```text
roles
-------------------------
id
name
```

Relación:

```text
user_roles
-------------------------
user_id
role_id
```

Roles:

```text
ADMIN
RESTAURANT_OPERATOR
DISPATCHER
DRIVER
```

---

## 11. Outbox

```text
outbox_events
-------------------------
id
aggregate_id
aggregate_type
event_type
payload
status
created_at
published_at
retry_count
```

Estados:

```text
PENDING
PUBLISHED
FAILED
```

Permite desacoplar la transacción de negocio de la publicación de eventos.

---

## 12. Eventos procesados

```text
processed_events
-------------------------
event_id
event_type
processed_at
consumer
```

Objetivo:

- idempotencia.

---

## 13. Configuración de SLA

Puede mantenerse inicialmente en configuración de aplicación.

Si se desea administrar:

```text
sla_rules
-------------------------
id
priority
max_minutes
warning_minutes
active
```

Ejemplo:

```text
NORMAL   45   10
HIGH     35   10
CRITICAL 25    8
```

---

## 14. Relaciones principales

```text
users 1 ----- 0..1 drivers

drivers 1 ----- 0..1 vehicles

orders 1 ----- N delivery_assignments

drivers 1 ----- N delivery_assignments

orders 1 ----- N order_history
```

---

## 15. Diagrama lógico simplificado

```text
┌──────────┐
│  users   │
└────┬─────┘
     |
     | 1:0..1
     v
┌──────────┐
│ drivers  │
└────┬─────┘
     |
     | 1:N
     v
┌──────────────────────┐
│ delivery_assignments │
└──────────┬───────────┘
           |
           | N:1
           v
      ┌──────────┐
      │  orders  │
      └────┬─────┘
           |
           | 1:N
           v
   ┌───────────────┐
   │ order_history │
   └───────────────┘
```

---

## 16. Índices

Índices iniciales recomendados:

```sql
CREATE INDEX idx_orders_status
ON orders(status);

CREATE INDEX idx_orders_priority
ON orders(priority);

CREATE INDEX idx_orders_promised_delivery
ON orders(promised_delivery_at);

CREATE INDEX idx_drivers_status
ON drivers(status);

CREATE INDEX idx_assignment_order
ON delivery_assignments(order_id);

CREATE INDEX idx_assignment_driver
ON delivery_assignments(driver_id);

CREATE INDEX idx_history_order
ON order_history(order_id);
```

Índice compuesto útil:

```sql
CREATE INDEX idx_orders_operational_queue
ON orders(status, priority, promised_delivery_at);
```

Este índice beneficia búsquedas del dispatcher.

---

## 17. Restricciones

Ejemplos:

```text
orders.code UNIQUE
users.email UNIQUE
vehicles.plate UNIQUE
processed_events.event_id PRIMARY KEY
```

También se deben aplicar:

- NOT NULL;
- FKs;
- CHECK cuando sea útil;
- escalas monetarias correctas.

---

## 18. Dinero

No utilizar `float` o `double` para montos.

Java:

```text
BigDecimal
```

PostgreSQL:

```text
NUMERIC(12,2)
```

---

## 19. Coordenadas

Para el MVP:

```text
latitude NUMERIC
longitude NUMERIC
```

El cálculo de distancia puede realizarse mediante Haversine en aplicación.

Una evolución futura podría utilizar PostGIS.

No es necesario introducir PostGIS para este examen.

---

## 20. Concurrencia

Un riesgo real es que dos procesos intenten asignar el mismo repartidor.

Se recomienda optimistic locking:

```java
@Version
private Long version;
```

Aplicable a:

- Order;
- Driver;
- DeliveryAssignment.

Flujo:

```text
Proceso A lee Driver version=3
Proceso B lee Driver version=3

A actualiza -> version=4
B intenta actualizar version=3

=> OptimisticLockException
```

El segundo proceso debe reevaluar la asignación.

---

## 21. Transacciones

Casos que deberían ser atómicos:

### Asignar pedido

```text
crear delivery_assignment
+
actualizar order
+
actualizar driver
+
insertar history
+
insertar outbox_event
```

Todo en una transacción.

### Completar entrega

```text
actualizar order
+
actualizar assignment
+
actualizar driver
+
insertar history
+
insertar outbox_event
```

Todo en una transacción.

---

## 22. Eliminación

No todo debe eliminarse físicamente.

Ejemplos:

### Usuarios

Preferir:

```text
status = INACTIVE
```

### Drivers

Preferir:

```text
SUSPENDED / INACTIVE
```

### Orders

Un pedido que inició el flujo operativo no debería eliminarse.

Puede permitirse eliminación únicamente en estado borrador si existe.

La trazabilidad es más importante que ofrecer un botón rojo por tradición.

---

## 23. Auditoría

Campos comunes:

```text
created_at
updated_at
created_by
updated_by
```

Spring Data puede utilizar auditing:

```text
@CreatedDate
@LastModifiedDate
@CreatedBy
@LastModifiedBy
```

Para eventos operativos se mantiene `order_history`.

---

## 24. Migraciones

Se recomienda Flyway.

```text
db/migration

V1__create_users.sql
V2__create_drivers.sql
V3__create_vehicles.sql
V4__create_orders.sql
V5__create_assignments.sql
V6__create_order_history.sql
V7__create_outbox.sql
```

Nunca depender únicamente de:

```text
hibernate.ddl-auto=update
```

en ambientes que pretendan ser reproducibles.

---

## 25. Seeds

Para demo se recomienda un set controlado.

### Usuarios

```text
admin@fleetbite.local
dispatcher@fleetbite.local
operator@fleetbite.local
driver1@fleetbite.local
driver2@fleetbite.local
```

### Drivers

```text
DRV-001 AVAILABLE
DRV-002 AVAILABLE
DRV-003 DELIVERING
```

### Pedidos

- 2 PREPARING;
- 2 READY;
- 1 WAITING_FOR_DRIVER;
- 3 DELIVERED.

Esto permite demostrar el dashboard desde el primer arranque.

---

## 26. Backup

Para el examen basta documentar:

```text
pg_dump
```

En producción:

- backups automáticos;
- snapshots;
- política de retención;
- restauración probada.

---

## 27. Datos sensibles

No almacenar:

- tokens en texto plano;
- passwords sin hash;
- secretos AWS;
- información innecesaria del cliente.

Passwords:

```text
BCrypt o Argon2
```

---

## 28. Modelo mínimo viable

Para entregar a tiempo, las tablas imprescindibles son:

```text
users
drivers
vehicles
orders
delivery_assignments
order_history
```

Si se implementan eventos:

```text
outbox_events
processed_events
```

---

## 29. Criterio de éxito

La persistencia debe permitir reconstruir:

```text
qué ocurrió
cuándo ocurrió
quién intervino
qué repartidor fue asignado
si hubo rechazo
si existió reasignación
cuánto tardó la entrega
```

Esa trazabilidad es más valiosa que multiplicar tablas sin necesidad.
