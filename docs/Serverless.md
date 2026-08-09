# FleetBite - Arquitectura Serverless y Procesamiento de Eventos

## 1. Propósito

Este documento define la estrategia serverless de FleetBite.

El objetivo no es convertir toda la aplicación en serverless, sino desacoplar procesos que:

- son asíncronos;
- se ejecutan por eventos;
- pueden presentar picos de carga;
- no deben bloquear el flujo transaccional principal;
- requieren reintentos controlados;
- pueden evolucionar independientemente del backend principal.

El núcleo transaccional continuará en Spring Boot. La capa serverless se utilizará para procesos operativos específicos, principalmente:

1. asignación automática de pedidos;
2. monitoreo de SLA;
3. notificaciones;
4. procesamiento de eventos de ubicación, si se implementa;
5. reintentos y manejo de excepciones.

---

## 2. Objetivo arquitectónico

La arquitectura propuesta es híbrida:

```text
Frontend
   |
   v
Spring Boot
   |
   +---- PostgreSQL
   |
   +---- Domain Events
             |
             v
      EventBridge / SQS
             |
             v
          Lambda
             |
             +---- Assignment Processor
             +---- SLA Monitor
             +---- Notification Processor
```

Spring Boot conserva:

- consistencia transaccional;
- reglas de dominio;
- CRUD;
- autenticación;
- autorización;
- persistencia principal;
- historial operativo.

Serverless se encarga de trabajo desacoplado y reactivo.

---

## 3. Principio principal

Los servicios AWS no deben contener reglas fundamentales que solo existan allí.

La regla de negocio debe permanecer expresada mediante contratos y casos de uso claros.

La Lambda debe actuar como consumidor/orquestador:

```text
Evento
  |
  v
Lambda
  |
  v
Puerto / API de aplicación
  |
  v
Caso de uso
  |
  v
Dominio
```

Esto evita que FleetBite dependa estructuralmente de AWS.

---

## 4. Eventos principales

### 4.1 ORDER_READY

Se genera cuando un pedido pasa a `READY`.

Payload conceptual:

```json
{
  "eventId": "evt-uuid",
  "eventType": "ORDER_READY",
  "occurredAt": "2026-08-10T18:20:00Z",
  "orderId": "ORD-2026-0012",
  "restaurantId": "REST-001",
  "priority": "NORMAL",
  "delivery": {
    "latitude": -12.1042,
    "longitude": -77.0305
  }
}
```

Responsabilidad:

- iniciar el proceso de búsqueda de repartidor.

---

### 4.2 DRIVER_ASSIGNMENT_REJECTED

Se genera cuando el repartidor rechaza una asignación.

```json
{
  "eventId": "evt-uuid",
  "eventType": "DRIVER_ASSIGNMENT_REJECTED",
  "orderId": "ORD-2026-0012",
  "driverId": "DRV-009",
  "assignmentId": "ASN-332",
  "occurredAt": "2026-08-10T18:24:00Z"
}
```

Responsabilidad:

- liberar asignación;
- volver el pedido a cola;
- iniciar un nuevo intento.

---

### 4.3 DRIVER_UNAVAILABLE

Se genera cuando un repartidor deja de estar disponible teniendo una asignación pendiente.

Responsabilidad:

- identificar pedidos comprometidos;
- evaluar reasignación.

---

### 4.4 ORDER_DELIVERY_AT_RISK

Se genera cuando un pedido tiene alta probabilidad de incumplir su SLA.

Responsabilidad:

- elevar visibilidad;
- notificar al dispatcher;
- incrementar prioridad si la regla lo permite.

---

### 4.5 ORDER_DELIVERED

Se genera cuando una entrega termina correctamente.

Responsabilidad:

- cerrar métricas;
- calcular tiempos;
- actualizar utilización de flota;
- disparar procesos secundarios no críticos.

---

## 5. Flujo de asignación automática

```text
Order pasa a READY
        |
        v
Spring Boot confirma transacción
        |
        v
Se publica ORDER_READY
        |
        v
EventBridge
        |
        v
SQS assignment-queue
        |
        v
Assignment Lambda
        |
        v
Consultar repartidores disponibles
        |
        v
Calcular candidato
        |
        +------ no existe ------> WAITING_FOR_DRIVER
        |
        v
Crear asignación
        |
        v
Pedido ASSIGNED
```

Se recomienda introducir SQS entre EventBridge y Lambda para:

- absorber picos;
- controlar reintentos;
- evitar pérdida de eventos;
- desacoplar productores y consumidores.

---

## 6. Estrategia de asignación

La Lambda no debe implementar una selección arbitraria.

Debe utilizar un caso de uso o servicio de dominio.

Factores propuestos:

```text
availability
distance
active deliveries
order priority
SLA remaining time
```

Versión inicial:

```text
score =
normalizedDistance * 0.60
+
normalizedWorkload * 0.25
+
normalizedSLARisk * 0.15
```

El candidato con menor score es seleccionado.

Para el MVP también puede simplificarse a:

1. filtrar `AVAILABLE`;
2. ordenar por distancia;
3. desempatar por menor carga;
4. seleccionar el primero.

La regla debe documentarse y ser determinística.

---

## 7. Idempotencia

Los sistemas distribuidos pueden entregar un mismo evento más de una vez.

Por ello, cada evento debe tener un identificador único:

```text
eventId
```

Antes de procesar:

```text
¿eventId ya procesado?
      |
   Sí | No
      | 
      v
 ignorar / procesar
```

Tabla sugerida:

```text
processed_events

event_id
event_type
processed_at
```

También puede utilizarse una estrategia equivalente mediante almacenamiento especializado.

El objetivo es evitar:

- doble asignación;
- doble notificación;
- cambios repetidos;
- registros duplicados.

---

## 8. Transactional Outbox

Existe un problema clásico:

```text
Guardar pedido en PostgreSQL
        |
        X
Publicar evento falla
```

El pedido podría quedar `READY`, pero sin evento de asignación.

Para evitarlo, se propone un patrón Transactional Outbox.

Dentro de la misma transacción:

```text
UPDATE orders
INSERT outbox_events
COMMIT
```

Luego un publicador independiente procesa:

```text
outbox_events
      |
      v
EventBridge / SQS
```

Estructura:

```text
outbox_events

id
aggregate_id
aggregate_type
event_type
payload
created_at
published_at
status
```

Para el examen, puede implementarse una versión sencilla o documentarse como decisión de robustez si el tiempo es limitado.

---

## 9. Colas

### assignment-queue

Eventos relacionados con asignación.

```text
ORDER_READY
DRIVER_ASSIGNMENT_REJECTED
DRIVER_UNAVAILABLE
```

### notification-queue

Eventos que generan notificaciones.

```text
ORDER_ASSIGNED
ORDER_DELIVERY_AT_RISK
DELIVERY_FAILED
```

No es necesario crear múltiples colas en el MVP si agrega complejidad innecesaria.

Una única cola operativa es suficiente para demostrar el concepto.

---

## 10. Dead Letter Queue

Cada cola importante puede tener una DLQ.

```text
assignment-queue
      |
      | failed after N retries
      v
assignment-dlq
```

La DLQ sirve para conservar eventos que no pudieron procesarse.

Ejemplos:

- backend no disponible;
- payload inválido;
- error de persistencia;
- inconsistencia no recuperable.

Los eventos de DLQ requieren revisión.

---

## 11. Retries

Los errores deben diferenciarse.

### Errores transitorios

Ejemplos:

- timeout;
- conexión temporal;
- 503.

Acción:

```text
retry
```

### Errores funcionales

Ejemplo:

```text
pedido ya entregado
```

Acción:

```text
no retry
```

### Errores inesperados

Acción:

```text
retry limitado
→ DLQ
```

Nunca se recomienda un reintento infinito. La eternidad es un pésimo mecanismo de recuperación.

---

## 12. SLA Monitor

Los pedidos activos deben revisarse periódicamente.

Una Lambda programada puede ejecutarse mediante EventBridge Scheduler.

```text
EventBridge Scheduler
       |
       | cada 5 minutos
       v
SLA Monitor Lambda
       |
       v
Buscar pedidos activos
       |
       v
Calcular tiempo restante
       |
       +---- SAFE
       +---- AT_RISK
       +---- BREACHED
```

Ejemplo:

```text
Promesa: 45 min
Transcurrido: 38 min
Estado: READY

Remaining: 7 min
Result: AT_RISK
```

La Lambda puede generar:

```text
ORDER_DELIVERY_AT_RISK
```

El intervalo final puede ajustarse. Para demo, 1 minuto puede ser útil. Para una implementación real dependería de costos y necesidades operativas.

---

## 13. Notificaciones

Se propone desacoplar notificaciones.

```text
Event
  |
  v
Notification Lambda
  |
  +---- email
  +---- SNS
  +---- push futuro
```

Eventos relevantes:

- pedido asignado;
- pedido en riesgo;
- delivery fallido;
- pedido esperando repartidor demasiado tiempo.

Para el MVP puede utilizarse logging estructurado o correo simple si configurar SNS consume demasiado tiempo.

---

## 14. Observabilidad

Cada Lambda debe registrar:

```text
eventId
correlationId
orderId
driverId
eventType
result
processingTime
```

Ejemplo conceptual:

```json
{
  "level": "INFO",
  "eventId": "evt-123",
  "orderId": "ORD-22",
  "processor": "assignment",
  "result": "ASSIGNED",
  "driverId": "DRV-04",
  "durationMs": 84
}
```

Servicios:

- CloudWatch Logs;
- CloudWatch Metrics;
- alarmas básicas;
- DLQ metrics.

---

## 15. Correlation ID

Se recomienda propagar un identificador de correlación:

```text
correlationId
```

Flujo:

```text
HTTP Request
  |
Spring Boot
  |
Domain Event
  |
SQS
  |
Lambda
```

El mismo ID permite reconstruir el recorrido completo.

---

## 16. Seguridad

Las Lambdas no deben exponer endpoints públicos innecesarios.

Principios:

- IAM con menor privilegio;
- secretos fuera del código;
- acceso controlado a colas;
- API Gateway protegido si recibe eventos externos;
- variables sensibles mediante Secrets Manager o Parameter Store;
- validación estricta de payload.

---

## 17. Infraestructura mínima para el examen

MVP serverless recomendado:

```text
1 EventBridge
1 SQS
1 DLQ
1 Assignment Lambda
1 SLA Monitor Lambda
```

Opcional:

```text
1 Notification Lambda
```

Esto es suficiente para demostrar:

- eventos;
- procesamiento desacoplado;
- reintentos;
- serverless;
- programación temporal.

---

## 18. Simulación local

Para desarrollo pueden utilizarse dos estrategias.

### Opción A: mocks y perfiles locales

Spring Boot publica a un adaptador local.

```text
DomainEventPublisherPort
        |
        +---- AwsEventPublisherAdapter
        |
        +---- LocalEventPublisherAdapter
```

### Opción B: LocalStack

Puede simular:

- SQS;
- EventBridge;
- Lambda;
- SNS.

Esta opción es interesante, pero no debe desplazar el desarrollo del core.

---

## 19. Criterio de éxito

La arquitectura serverless se considera funcional si permite demostrar:

```text
Pedido READY
    |
evento
    |
cola
    |
Lambda
    |
asignación
    |
pedido ASSIGNED
```

y un escenario alternativo:

```text
Pedido READY
    |
sin driver
    |
WAITING_FOR_DRIVER
    |
nuevo evento / retry
    |
asignación posterior
```

---

## 20. Decisiones que deben defenderse

### ¿Por qué no hacer toda la aplicación Lambda?

Porque el core mantiene:

- operaciones transaccionales;
- múltiples casos de uso;
- reglas de dominio;
- persistencia;
- autenticación;
- endpoints CRUD.

Spring Boot es adecuado para ese núcleo.

### ¿Por qué serverless para asignación?

Porque la asignación es:

- disparada por eventos;
- desacoplable;
- escalable;
- no necesita bloquear la petición que cambia el pedido a `READY`.

### ¿Por qué SQS?

Para:

- desacoplar;
- absorber picos;
- permitir retries;
- evitar perder eventos.

### ¿Por qué una DLQ?

Porque un evento no procesable no debe desaparecer silenciosamente.

---

## 21. Resumen

FleetBite utilizará serverless como una extensión del backend, no como sustituto del mismo.

La arquitectura objetivo es:

```text
Spring Boot
   |
Domain Event
   |
EventBridge
   |
SQS
   |
Lambda
   |
Business Use Case
   |
PostgreSQL
```

Este diseño permite demostrar una arquitectura orientada a eventos con propósito real dentro del proceso de delivery.
