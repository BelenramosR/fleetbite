# FleetBite Backend

## Diseño técnico del backend con Java Spring Boot y Arquitectura Hexagonal

## 1. Propósito del documento

Este documento define la propuesta técnica del backend de **FleetBite**, plataforma de gestión operativa de pedidos, delivery y asignación inteligente de flota motorizada.

El backend será responsable de:

- exponer la API REST consumida por el frontend;
- gestionar el ciclo de vida de pedidos;
- administrar repartidores y vehículos;
- controlar las reglas de transición de estados;
- ejecutar y coordinar asignaciones de delivery;
- gestionar prioridades y SLA;
- mantener historial y trazabilidad;
- aplicar autenticación y autorización;
- persistir información transaccional;
- publicar eventos de dominio hacia procesos asíncronos;
- integrarse con componentes serverless sin acoplar el dominio a AWS u otro proveedor.

La propuesta prioriza separación de responsabilidades, testabilidad, mantenibilidad y capacidad de evolución.

---

## 2. Stack propuesto

### Backend principal

- **Java 21**
- **Spring Boot 3.x**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security**
- **Spring Validation**
- **Spring Actuator**
- **PostgreSQL**
- **Flyway** para migraciones
- **MapStruct** para mapeo entre modelos
- **Lombok** de manera limitada y controlada
- **OpenAPI / Swagger** para documentación de endpoints
- **JUnit 5**
- **Mockito**
- **Testcontainers** para pruebas de integración con PostgreSQL
- **Docker**

### Integraciones asíncronas

Según el alcance final:

- **AWS EventBridge** o **Amazon SQS** para mensajería;
- **AWS Lambda** para procesamiento serverless;
- **AWS SNS** o un proveedor de correo para notificaciones.

El núcleo de negocio no dependerá directamente del SDK de AWS. Las integraciones externas se implementarán mediante adaptadores.

---

## 3. Decisión arquitectónica

Se propone utilizar **Arquitectura Hexagonal**, también conocida como **Ports and Adapters**.

La intención principal es evitar que la lógica de negocio dependa de elementos de infraestructura como:

- Spring MVC;
- PostgreSQL;
- JPA;
- AWS;
- servicios de mapas;
- proveedores de notificaciones;
- mecanismos específicos de autenticación.

El dominio debe poder expresar el comportamiento del negocio independientemente de esas tecnologías.

La estructura conceptual será:

```text
                ┌─────────────────────────────┐
                │       Driving Adapters      │
                │ REST / Events / Scheduler   │
                └──────────────┬──────────────┘
                               │
                         Input Ports
                               │
                ┌──────────────▼──────────────┐
                │      Application Layer      │
                │        Use Cases            │
                └──────────────┬──────────────┘
                               │
                            Domain
                               │
                ┌──────────────▼──────────────┐
                │      Business Rules         │
                │ Entities / Value Objects    │
                └──────────────┬──────────────┘
                               │
                        Output Ports
                               │
                ┌──────────────▼──────────────┐
                │       Driven Adapters       │
                │ JPA / AWS / Mail / Maps     │
                └─────────────────────────────┘
```

---

## 4. Principios de diseño

### 4.1 El dominio controla las reglas

Reglas como las siguientes no deben vivir únicamente en controladores ni en el frontend:

- un pedido entregado no puede volver a preparación;
- un pedido cancelado no puede ser asignado;
- un repartidor no disponible no puede recibir una nueva asignación;
- un pedido solo puede marcarse como recogido si ya tiene una asignación activa;
- un pedido listo debe pasar por el flujo de asignación;
- una asignación rechazada debe liberar al repartidor y volver a poner el pedido en espera;
- un pedido en riesgo puede incrementar su prioridad.

### 4.2 Los casos de uso coordinan, no contienen todo el dominio

Los servicios de aplicación serán responsables de orquestar el flujo:

```text
Controller
    ↓
Use Case
    ↓
Domain
    ↓
Repository Port
    ↓
Persistence Adapter
```

### 4.3 La infraestructura es reemplazable

La aplicación puede comenzar con PostgreSQL y AWS, pero el dominio no debe asumir que esos proveedores existirán siempre.

### 4.4 Las operaciones importantes son auditables

Los cambios relevantes deben generar un registro histórico.

Ejemplos:

- cambio de estado;
- asignación;
- rechazo de asignación;
- reasignación;
- cancelación;
- entrega fallida;
- cierre de entrega.

---

## 5. Organización propuesta del proyecto

Se recomienda organizar el backend por **módulos de negocio**, no únicamente por tipo técnico global.

Ejemplo:

```text
src/main/java/com/fleetbite
│
├── order
│   ├── domain
│   ├── application
│   └── infrastructure
│
├── delivery
│   ├── domain
│   ├── application
│   └── infrastructure
│
├── driver
│   ├── domain
│   ├── application
│   └── infrastructure
│
├── vehicle
│   ├── domain
│   ├── application
│   └── infrastructure
│
├── identity
│   ├── domain
│   ├── application
│   └── infrastructure
│
├── monitoring
│   ├── application
│   └── infrastructure
│
└── shared
    ├── domain
    ├── application
    └── infrastructure
```

Esta organización facilita que cada módulo tenga su propio lenguaje y responsabilidades.

---

## 6. Estructura interna por módulo

Ejemplo para `order`:

```text
order/
├── domain/
│   ├── model/
│   │   ├── Order.java
│   │   ├── OrderId.java
│   │   ├── OrderStatus.java
│   │   ├── OrderPriority.java
│   │   └── DeliveryAddress.java
│   │
│   ├── service/
│   │   └── OrderPriorityPolicy.java
│   │
│   ├── event/
│   │   ├── OrderReadyEvent.java
│   │   ├── OrderDeliveredEvent.java
│   │   └── OrderCancelledEvent.java
│   │
│   └── exception/
│       ├── InvalidOrderTransitionException.java
│       └── OrderNotFoundException.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateOrderUseCase.java
│   │   │   ├── UpdateOrderStatusUseCase.java
│   │   │   └── GetOrderUseCase.java
│   │   │
│   │   └── out/
│   │       ├── OrderRepositoryPort.java
│   │       └── DomainEventPublisherPort.java
│   │
│   ├── service/
│   │   ├── CreateOrderService.java
│   │   ├── UpdateOrderStatusService.java
│   │   └── GetOrderService.java
│   │
│   └── dto/
│       └── ...
│
└── infrastructure/
    ├── inbound/
    │   └── rest/
    │       ├── OrderController.java
    │       ├── request/
    │       └── response/
    │
    └── outbound/
        ├── persistence/
        │   ├── OrderJpaEntity.java
        │   ├── SpringDataOrderRepository.java
        │   ├── OrderPersistenceAdapter.java
        │   └── OrderPersistenceMapper.java
        │
        └── events/
            └── AwsEventPublisherAdapter.java
```

---

## 7. Módulos funcionales

## 7.1 Order Management

Responsable del ciclo de vida del pedido.

Casos de uso principales:

- crear pedido;
- obtener pedido;
- listar pedidos;
- actualizar información editable;
- confirmar pedido;
- iniciar preparación;
- marcar pedido como listo;
- cancelar pedido;
- consultar historial.

---

## 7.2 Driver Management

Responsable de administrar repartidores.

Casos de uso:

- registrar repartidor;
- actualizar información;
- activar/desactivar repartidor;
- cambiar disponibilidad;
- consultar ubicación actual;
- listar repartidores disponibles;
- consultar pedidos activos.

Estados sugeridos:

```text
OFFLINE
AVAILABLE
RESERVED
DELIVERING
UNAVAILABLE
```

`RESERVED` puede utilizarse entre la asignación y la aceptación definitiva para evitar carreras de asignación.

---

## 7.3 Vehicle Management

Responsable de vehículos asociados a la flota.

Casos de uso:

- registrar vehículo;
- editar vehículo;
- cambiar estado;
- asignar vehículo a repartidor;
- consultar disponibilidad.

Estados:

```text
AVAILABLE
IN_USE
MAINTENANCE
INACTIVE
```

---

## 7.4 Delivery Assignment

Será uno de los módulos con mayor valor técnico.

Responsabilidades:

- recibir un pedido listo;
- buscar candidatos disponibles;
- calcular un score de asignación;
- reservar un repartidor;
- crear la asignación;
- procesar aceptación o rechazo;
- reasignar cuando sea necesario;
- gestionar pedidos en espera.

---

## 7.5 SLA Monitoring

Responsable de evaluar el riesgo temporal de los pedidos activos.

Puede considerar:

- tiempo transcurrido desde creación;
- tiempo transcurrido desde que quedó listo;
- tiempo prometido de entrega;
- estado actual;
- disponibilidad de repartidores;
- tiempo estimado restante.

Debe poder clasificar un pedido como:

```text
ON_TIME
AT_RISK
LATE
```

---

## 7.6 Audit / Timeline

Responsable de mantener trazabilidad.

Eventos relevantes:

```text
ORDER_CREATED
ORDER_CONFIRMED
ORDER_PREPARING
ORDER_READY
DRIVER_ASSIGNED
ASSIGNMENT_ACCEPTED
ASSIGNMENT_REJECTED
ORDER_PICKED_UP
ORDER_IN_TRANSIT
ORDER_DELIVERED
ORDER_CANCELLED
DELIVERY_FAILED
ORDER_REASSIGNED
```

---

## 8. Modelo de dominio principal

## 8.1 Order

Atributos conceptuales:

```text
Order
- id
- code
- customerName
- customerPhone
- deliveryAddress
- latitude
- longitude
- totalAmount
- priority
- status
- promisedDeliveryAt
- createdAt
- confirmedAt
- preparingAt
- readyAt
- assignedAt
- pickedUpAt
- deliveredAt
- cancelledAt
- version
```

`version` puede utilizarse para optimistic locking.

---

## 8.2 Driver

```text
Driver
- id
- userId
- name
- phone
- status
- latitude
- longitude
- lastLocationUpdateAt
- activeOrderCount
- vehicleId
- version
```

---

## 8.3 Vehicle

```text
Vehicle
- id
- plate
- type
- status
- brand
- model
```

---

## 8.4 DeliveryAssignment

```text
DeliveryAssignment
- id
- orderId
- driverId
- assignedAt
- acceptedAt
- rejectedAt
- completedAt
- status
- assignmentScore
- rejectionReason
```

Estados:

```text
PENDING_ACCEPTANCE
ACCEPTED
REJECTED
CANCELLED
COMPLETED
```

---

## 8.5 OrderTimelineEvent

```text
OrderTimelineEvent
- id
- orderId
- eventType
- previousStatus
- newStatus
- performedBy
- metadata
- createdAt
```

---

## 9. Máquina de estados del pedido

Las transiciones deben ser explícitas.

Propuesta:

```text
CREATED
   ↓
CONFIRMED
   ↓
PREPARING
   ↓
READY
   ├──→ WAITING_FOR_DRIVER
   │           ↓
   │        ASSIGNED
   │
   └────────→ ASSIGNED
               ↓
            PICKED_UP
               ↓
            IN_TRANSIT
               ↓
            DELIVERED
```

Cancelaciones permitidas:

```text
CREATED → CANCELLED
CONFIRMED → CANCELLED
PREPARING → CANCELLED
READY → CANCELLED
WAITING_FOR_DRIVER → CANCELLED
```

Una vez en `PICKED_UP`, la cancelación debería requerir un flujo excepcional distinto.

---

## 10. Reglas de negocio principales

### BR-001

Un pedido solo puede marcarse como `READY` si se encuentra en `PREPARING`.

### BR-002

Un pedido `READY` debe generar una solicitud de asignación.

### BR-003

Un repartidor solo puede recibir una asignación si está `AVAILABLE`.

### BR-004

Un repartidor reservado para una asignación no puede ser considerado simultáneamente por otro proceso de asignación.

### BR-005

Un pedido solo puede tener una asignación activa.

### BR-006

Si un repartidor rechaza una asignación, el pedido debe volver al mecanismo de despacho.

### BR-007

Si no existen repartidores disponibles, el pedido debe pasar a `WAITING_FOR_DRIVER`.

### BR-008

Un pedido entregado no puede cambiar nuevamente a un estado operativo anterior.

### BR-009

Toda transición de estado relevante debe quedar registrada en el timeline.

### BR-010

Un pedido que supera su tiempo prometido pasa a estado SLA `LATE`.

### BR-011

Un pedido cuyo tiempo restante cae debajo de un umbral configurable puede pasar a `AT_RISK`.

### BR-012

La reasignación manual debe guardar quién la realizó y el motivo.

---

## 11. Motor de asignación

La primera versión debe ser determinística y explicable.

No usar machine learning.

### Datos considerados

- disponibilidad del repartidor;
- distancia aproximada entre repartidor y punto de recojo;
- cantidad de pedidos activos;
- antigüedad del pedido;
- prioridad del pedido;
- SLA restante.

### Fórmula inicial sugerida

Se puede utilizar un costo ponderado:

```text
assignmentCost =
    normalizedDistance * W_DISTANCE
  + normalizedWorkload * W_WORKLOAD
  + normalizedDelayRisk * W_DELAY
```

El repartidor con menor costo será candidato preferente.

Ejemplo de pesos iniciales:

```text
W_DISTANCE = 0.50
W_WORKLOAD = 0.20
W_DELAY    = 0.30
```

Los pesos deben ser configurables.

### Importante

La fórmula no tiene que ser perfecta. Para la evaluación es más valioso que sea:

- coherente;
- entendible;
- testeable;
- configurable;
- fácilmente reemplazable.

---

## 12. Distancia entre puntos

Para el MVP se puede evitar integrar un proveedor externo de mapas.

Puede utilizarse la fórmula de **Haversine** para estimar distancia geográfica entre dos coordenadas.

Entrada:

```text
Driver latitude/longitude
Restaurant latitude/longitude
```

Salida:

```text
DistanceKm
```

Una integración futura podría reemplazar este cálculo por Google Maps, Mapbox u otro proveedor para considerar distancia vial y tráfico.

La arquitectura hexagonal permite que esto sea un puerto:

```java
public interface DistanceCalculatorPort {
    Distance calculate(Location origin, Location destination);
}
```

Implementación MVP:

```text
HaversineDistanceAdapter
```

Implementación futura:

```text
GoogleMapsDistanceAdapter
```

---

## 13. Concurrencia en asignaciones

Este punto debe cuidarse porque dos pedidos pueden intentar asignar al mismo repartidor simultáneamente.

### Riesgo

```text
Order A → Driver 5 AVAILABLE
Order B → Driver 5 AVAILABLE
```

Ambos procesos podrían seleccionar al mismo driver antes de que su estado cambie.

### Estrategia inicial

Utilizar:

- transacciones;
- optimistic locking mediante `@Version`;
- estado intermedio `RESERVED`;
- reintento controlado cuando exista conflicto.

Flujo:

```text
buscar candidato
↓
intentar reservar driver
↓
¿version válida?
   ├─ Sí → continuar
   └─ No → buscar siguiente candidato
```

Para un MVP esto es más razonable que introducir locks distribuidos.

---

## 14. Consistencia transaccional y eventos

Cuando un pedido pase a `READY`, necesitamos:

1. persistir el cambio;
2. generar el evento `ORDER_READY`.

No se recomienda publicar primero hacia AWS y guardar después.

Para una versión robusta se propone **Transactional Outbox Pattern**.

```text
Transaction
│
├── UPDATE orders
└── INSERT outbox_event

COMMIT

Outbox Publisher
↓
EventBridge / SQS
```

Beneficio:

Evita que la base de datos confirme el cambio pero el evento se pierda, o viceversa.

Para el examen existen dos opciones:

### Opción A. MVP simple

Publicar evento después del commit mediante Spring events.

### Opción B. Valor agregado

Implementar Outbox Pattern.

Si el tiempo es limitado, la opción A es suficiente y el Outbox puede documentarse como evolución.

---

## 15. Procesamiento asíncrono y serverless

El backend principal permanecerá como aplicación Spring Boot.

Los procesos event-driven pueden delegarse.

### Flujo ORDER_READY

```text
Spring Boot
↓
ORDER_READY
↓
EventBridge / SQS
↓
Lambda Assignment Trigger
↓
API / comando de asignación
```

Alternativamente, la Lambda puede contener el algoritmo de asignación, pero para mantener el dominio centralizado se recomienda que la lógica principal siga perteneciendo al dominio Spring Boot.

Una opción limpia es que la Lambda coordine el evento y llame a un endpoint interno de comando.

### Flujo SLA

```text
EventBridge Scheduler
↓
Lambda SLA Monitor
↓
consulta pedidos activos
↓
identifica AT_RISK / LATE
↓
actualiza backend / publica alertas
```

---

## 16. API REST propuesta

Base URL:

```text
/api/v1
```

### Pedidos

```text
POST   /orders
GET    /orders
GET    /orders/{id}
PATCH  /orders/{id}
POST   /orders/{id}/confirm
POST   /orders/{id}/start-preparation
POST   /orders/{id}/ready
POST   /orders/{id}/cancel
GET    /orders/{id}/timeline
```

Se recomienda modelar acciones importantes como comandos explícitos, en lugar de permitir modificar arbitrariamente `status` mediante un `PATCH` genérico.

Esto:

```text
POST /orders/{id}/ready
```

es preferible a:

```json
PATCH /orders/{id}
{
  "status": "READY"
}
```

porque expresa intención de negocio y permite validar reglas específicas.

### Repartidores

```text
POST   /drivers
GET    /drivers
GET    /drivers/{id}
PATCH  /drivers/{id}
POST   /drivers/{id}/activate
POST   /drivers/{id}/deactivate
POST   /drivers/{id}/availability
PUT    /drivers/{id}/location
GET    /drivers/available
```

### Vehículos

```text
POST   /vehicles
GET    /vehicles
GET    /vehicles/{id}
PATCH  /vehicles/{id}
POST   /vehicles/{id}/assign-driver
```

### Asignaciones

```text
GET    /assignments
GET    /assignments/{id}
POST   /orders/{orderId}/assignments/auto
POST   /orders/{orderId}/assignments/manual
POST   /assignments/{id}/accept
POST   /assignments/{id}/reject
POST   /assignments/{id}/cancel
```

### Delivery

```text
POST   /orders/{orderId}/pickup
POST   /orders/{orderId}/start-delivery
POST   /orders/{orderId}/deliver
POST   /orders/{orderId}/delivery-failed
```

### Dashboard

```text
GET /dashboard/operations
GET /dashboard/fleet
GET /dashboard/sla
```

---

## 17. Contrato de respuesta

Se recomienda una estructura consistente.

Respuesta exitosa:

```json
{
  "data": {
    "id": "ord_123",
    "code": "ORD-2026-000123",
    "status": "READY"
  }
}
```

Lista paginada:

```json
{
  "data": [],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 153,
    "totalPages": 8
  }
}
```

Error:

```json
{
  "code": "INVALID_ORDER_TRANSITION",
  "message": "The order cannot transition from DELIVERED to PREPARING",
  "timestamp": "2026-08-08T18:00:00Z",
  "path": "/api/v1/orders/123/start-preparation"
}
```

---

## 18. Manejo global de errores

Utilizar `@RestControllerAdvice`.

Categorías sugeridas:

```text
400 BAD_REQUEST
401 UNAUTHORIZED
403 FORBIDDEN
404 NOT_FOUND
409 CONFLICT
422 UNPROCESSABLE_ENTITY
500 INTERNAL_SERVER_ERROR
```

Casos como asignación concurrente o transición inválida pueden representarse como `409 Conflict`.

---

## 19. Validaciones

### Validaciones estructurales

Con Bean Validation:

```java
@NotBlank
@Email
@Positive
@Size
```

### Validaciones de negocio

Deben estar en dominio o aplicación.

Ejemplo:

```text
Un pedido DELIVERED no puede cancelarse.
```

Esto no debe depender de una anotación en un DTO.

---

## 20. Persistencia

### Base de datos

**PostgreSQL**.

Razones:

- modelo relacional adecuado;
- soporte transaccional;
- constraints;
- índices;
- JSONB disponible para metadata de auditoría;
- buen soporte con JPA y Testcontainers.

---

## 21. Tablas iniciales

```text
users
roles
user_roles

drivers
vehicles

orders
delivery_assignments
order_timeline_events

outbox_events        opcional
refresh_tokens       opcional
```

---

## 22. Esquema conceptual

```text
users
  1
  │
  │
  1
 drivers ─────── vehicles
    │
    │ 1
    │
    N
 delivery_assignments
    │
    │ N
    │
    1
  orders
    │
    │ 1
    │
    N
 order_timeline_events
```

---

## 23. Índices importantes

Ejemplos:

```text
orders(status)
orders(priority)
orders(promised_delivery_at)
orders(created_at)

drivers(status)
drivers(last_location_update_at)

delivery_assignments(order_id, status)
delivery_assignments(driver_id, status)

order_timeline_events(order_id, created_at)
```

También deben existir constraints para asegurar invariantes simples.

---

## 24. Migraciones

Utilizar **Flyway**.

Ejemplo:

```text
V1__create_users.sql
V2__create_drivers_and_vehicles.sql
V3__create_orders.sql
V4__create_delivery_assignments.sql
V5__create_order_timeline.sql
```

No depender de:

```text
spring.jpa.hibernate.ddl-auto=create
```

para entornos compartidos.

---

## 25. Seguridad

### Autenticación

```text
Spring Security + JWT
```

Flujo:

```text
POST /auth/login
↓
credenciales
↓
JWT access token
↓
frontend
↓
Authorization: Bearer <token>
```

### Roles

```text
ADMIN
RESTAURANT_OPERATOR
DISPATCHER
DRIVER
```

### Ejemplos de autorización

```java
@PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_OPERATOR')")
```

```java
@PreAuthorize("hasRole('DISPATCHER')")
```

```java
@PreAuthorize("hasRole('DRIVER')")
```

Además de verificar rol, ciertas operaciones deben validar ownership.

Ejemplo:

Un repartidor solo debe poder aceptar una asignación creada para él.

---

## 26. Contraseñas

Utilizar:

```text
BCryptPasswordEncoder
```

Nunca almacenar contraseñas en texto plano.

---

## 27. Auditoría y trazabilidad

Cada acción relevante debe registrar:

```text
qué ocurrió
quién lo hizo
sobre qué pedido
estado anterior
estado nuevo
fecha y hora
metadata opcional
```

Ejemplo:

```json
{
  "eventType": "ASSIGNMENT_REJECTED",
  "performedBy": "driver_23",
  "metadata": {
    "reason": "VEHICLE_ISSUE"
  }
}
```

---

## 28. Observabilidad

Incluir **Spring Boot Actuator**.

Endpoints útiles:

```text
/actuator/health
/actuator/info
/actuator/metrics
```

Para producción se deben restringir mediante seguridad.

Logs estructurados deben incluir, cuando corresponda:

```text
orderId
driverId
assignmentId
correlationId
```

---

## 29. Correlation ID

Para rastrear operaciones entre componentes:

```text
Frontend
↓
Spring Boot
↓
EventBridge
↓
Lambda
```

se recomienda propagar:

```text
X-Correlation-Id
```

Esto facilita entender el recorrido de un evento durante la revisión técnica.

---

## 30. Idempotencia

Los eventos pueden recibirse más de una vez.

Por ejemplo:

```text
ORDER_READY
ORDER_READY
```

No deben producir dos asignaciones activas.

La aplicación debe validar:

```text
if order already has active assignment
    do not create another
```

Para eventos externos se puede almacenar un `eventId` procesado.

---

## 31. Paginación, filtros y ordenamiento

Los listados no deben devolver la base completa.

Ejemplo:

```text
GET /orders?page=0&size=20&status=READY&priority=HIGH&sort=createdAt,desc
```

Filtros relevantes:

- estado;
- prioridad;
- rango de fechas;
- repartidor;
- SLA;
- código de pedido.

---

## 32. Dashboard backend

El backend debe entregar métricas agregadas listas para consumo.

Ejemplo:

```json
{
  "activeOrders": 18,
  "waitingForDriver": 3,
  "inTransit": 9,
  "slaAtRisk": 2,
  "availableDrivers": 5,
  "averageAssignmentMinutes": 3.4,
  "averageDeliveryMinutes": 31.2,
  "fleetUtilizationPercentage": 75.0
}
```

Para el MVP las consultas agregadas pueden ejecutarse directamente en PostgreSQL.

---

## 33. Cache

No introducir Redis inicialmente salvo que exista una necesidad demostrable.

Para este examen, PostgreSQL y consultas bien indexadas son suficientes.

Una cache futura podría utilizarse para:

- disponibilidad de drivers;
- ubicación reciente;
- parámetros de configuración.

No conviene sumar infraestructura únicamente para inflar el diagrama.

---

## 34. Testing

La arquitectura hexagonal permite probar reglas sin levantar Spring.

### 34.1 Unit tests de dominio

Ejemplos:

```text
should_mark_order_ready_when_preparing
should_reject_invalid_transition_from_delivered_to_preparing
should_not_assign_unavailable_driver
should_move_order_to_waiting_when_no_driver_available
should_mark_order_at_risk_when_sla_threshold_is_reached
```

### 34.2 Unit tests del algoritmo

```text
should_select_closest_driver_when_workload_is_equal
should_penalize_driver_with_active_orders
should_not_include_offline_drivers
```

### 34.3 Integration tests

Con Spring Boot + Testcontainers PostgreSQL:

```text
repository persistence
REST controller
security
transaction boundaries
```

### 34.4 API tests

Flujos críticos:

```text
crear pedido
→ preparar
→ ready
→ asignar
→ aceptar
→ pickup
→ transit
→ delivered
```

---

## 35. Swagger / OpenAPI

La API debe poder revisarse desde Swagger UI.

Documentar:

- endpoints;
- parámetros;
- modelos;
- códigos HTTP;
- ejemplos;
- autenticación Bearer JWT.

Esto es especialmente útil para la evaluación porque permite probar el backend aunque el frontend tenga algún problema.

---

## 36. Docker

Crear una imagen para Spring Boot mediante multi-stage build.

Ejemplo conceptual:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

No es necesario usar exactamente este Dockerfile, pero el resultado debe ser una imagen reproducible.

---

## 37. Docker Compose

El backend local puede levantarse junto a PostgreSQL.

```text
services:
  postgres
  backend
```

Posteriormente:

```text
frontend
sensor/driver-simulator
localstack opcional
```

Variables sensibles deben llegar mediante variables de entorno.

---

## 38. Variables de configuración

Ejemplos:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
AWS_REGION
EVENT_BUS_NAME
SQS_QUEUE_URL
```

No guardar secretos en el repositorio.

---

## 39. Configuración operativa

Parámetros como los siguientes no deberían estar enterrados en código:

```text
assignment.distance.weight
assignment.workload.weight
assignment.delay.weight
sla.at-risk.minutes
assignment.acceptance.timeout.seconds
```

Se pueden mantener en `application.yml` durante el MVP.

---

## 40. Flujo técnico: pedido listo

```text
1. Operador llama POST /orders/{id}/ready
2. Controller ejecuta MarkOrderReadyUseCase
3. Use Case obtiene Order
4. Domain valida transición
5. Order cambia PREPARING → READY
6. Se registra timeline
7. Se persiste transacción
8. Se publica ORDER_READY
9. Dispatcher inicia búsqueda
10. Se selecciona candidato
11. Driver cambia AVAILABLE → RESERVED
12. Se crea DeliveryAssignment
13. Order cambia READY → ASSIGNED
14. Se registra timeline
```

Si no existe candidato:

```text
READY
↓
WAITING_FOR_DRIVER
```

---

## 41. Flujo técnico: rechazo

```text
Driver recibe asignación
↓
REJECT
↓
Assignment = REJECTED
↓
Driver = AVAILABLE
↓
Order = WAITING_FOR_DRIVER
↓
ORDER_REASSIGNMENT_REQUESTED
↓
Nuevo intento
```

Debe guardarse el motivo.

---

## 42. Flujo técnico: entrega

```text
ASSIGNED
↓
PICKED_UP
↓
IN_TRANSIT
↓
DELIVERED
```

Al entregar:

```text
Order → DELIVERED
Assignment → COMPLETED
Driver → AVAILABLE
Timeline → ORDER_DELIVERED
```

Estas modificaciones deben ejecutarse dentro de una transacción consistente.

---

## 43. Manejo de delivery fallido

Razones posibles:

```text
CUSTOMER_UNAVAILABLE
WRONG_ADDRESS
VEHICLE_ISSUE
ACCIDENT
ORDER_DAMAGED
OTHER
```

Flujo:

```text
IN_TRANSIT
↓
FAILED_DELIVERY
↓
Dispatcher review
```

No necesariamente debe volver automáticamente a `READY`, porque la resolución depende del motivo.

---

## 44. Soft delete

No se recomienda eliminar físicamente información histórica crítica.

Para entidades operativas se puede usar:

```text
active = false
```

o un campo similar.

Pedidos entregados, cancelados o con historial deben conservarse.

---

## 45. Fechas y zonas horarias

Persistir timestamps en UTC.

Ejemplo:

```text
Instant
OffsetDateTime
```

El frontend será responsable de presentar la hora en la zona correspondiente.

No depender de la zona horaria local del servidor.

---

## 46. Identificadores

Se puede utilizar UUID como identificador interno.

Ejemplo:

```text
id = UUID
```

Y mantener un código amigable:

```text
ORD-2026-000231
```

El código es para visualización; el UUID para relaciones técnicas.

---

## 47. Estrategia de implementación por prioridad

### Fase 1. Core

- estructura hexagonal;
- PostgreSQL;
- migraciones;
- CRUD de orders;
- CRUD de drivers;
- CRUD de vehicles;
- estados del pedido;
- Swagger.

### Fase 2. Operación delivery

- delivery assignments;
- asignación manual;
- asignación automática;
- Haversine;
- timeline;
- transacciones.

### Fase 3. Seguridad

- usuarios;
- JWT;
- roles;
- autorización.

### Fase 4. Valor agregado

- eventos;
- Lambda/SQS/EventBridge;
- SLA monitor;
- dashboard;
- observabilidad.

### Fase 5. Robustez

- idempotencia;
- optimistic locking;
- Testcontainers;
- Outbox Pattern si existe tiempo.

---

## 48. Alcance recomendado para el examen

### Debe funcionar

- crear y consultar pedidos;
- modificar estados mediante comandos válidos;
- administrar drivers;
- administrar vehicles;
- asignar un driver automáticamente;
- aceptar/rechazar asignación;
- completar entrega;
- visualizar timeline;
- seguridad básica;
- PostgreSQL;
- Swagger;
- Docker.

### Debe demostrar valor

- score de asignación;
- estado `WAITING_FOR_DRIVER`;
- reasignación;
- SLA `AT_RISK`;
- dashboard básico;
- al menos un flujo asíncrono.

### Puede documentarse como evolución

- batching de múltiples pedidos;
- tráfico en tiempo real;
- mapas externos;
- WebSockets;
- Outbox Pattern completo;
- caching distribuido;
- optimización de rutas;
- ML.

---

## 49. Decisiones que se deben poder defender en la revisión

### ¿Por qué Hexagonal?

Porque permite mantener reglas de negocio independientes de REST, JPA y AWS, facilitando pruebas y evolución.

### ¿Por qué PostgreSQL?

Porque el proceso es transaccional, posee relaciones claras y requiere consistencia e historial.

### ¿Por qué no microservicios?

Porque para este alcance incrementarían la complejidad operacional sin aportar un beneficio proporcional. Se propone un monolito modular con integración orientada a eventos.

### ¿Por qué serverless?

Porque procesos como asignación disparada por eventos y monitoreo periódico de SLA no necesitan acoplarse al flujo HTTP síncrono y pueden ejecutarse bajo demanda.

### ¿Por qué Docker?

Porque garantiza reproducibilidad del backend y la base de datos en diferentes ambientes.

### ¿Por qué no modificar `status` directamente?

Porque los cambios de estado representan acciones de negocio con reglas y efectos secundarios propios.

### ¿Por qué no IA para asignar?

Porque una política determinística es suficiente para el alcance, puede explicarse, probarse y ajustarse sin disponer de un dataset histórico.

---

## 50. Objetivo técnico final

El backend de FleetBite debe demostrar que es posible construir un sistema empresarial donde un CRUD tradicional sea únicamente la base de un flujo más amplio.

El valor técnico debe verse en:

```text
CRUD
+
reglas de dominio
+
máquina de estados
+
asignación automática
+
seguridad
+
trazabilidad
+
procesamiento asíncrono
+
virtualización
+
testing
```

La implementación debe mantenerse deliberadamente contenida. La calidad de las decisiones, separación de responsabilidades y consistencia del flujo tienen prioridad sobre agregar tecnologías que no solucionan un problema concreto.

---

## 51. Documentos relacionados

Este archivo forma parte de la documentación técnica de FleetBite.

Documentos propuestos:

```text
FleetBite_Solucion_General.md
FleetBite_Backend.md
FleetBite_Frontend.md
FleetBite_Serverless.md
FleetBite_Database.md
FleetBite_Deployment.md
```

El siguiente documento recomendado es **FleetBite_Frontend.md**, donde se definirá la decisión React vs Angular, estructura de módulos, gestión de estado, caching, estrategia de carga, Skeleton UI, dashboard y componentes de operación.
