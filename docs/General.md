# FleetBite

## Sistema de gestión operativa de pedidos, delivery y asignación inteligente de flota motorizada

## 1. Resumen ejecutivo

**FleetBite** es una plataforma web orientada a restaurantes, dark kitchens y negocios gastronómicos que operan con delivery propio. Su objetivo es gestionar de extremo a extremo el proceso de despacho de pedidos, desde que una orden queda lista para entrega hasta su asignación a un motorizado, seguimiento operativo y cierre.

La solución no se plantea como una aplicación de ecommerce ni como un clon de una plataforma de delivery. El foco está en la **operación logística de última milla** y en resolver problemas frecuentes de restaurantes que administran su propia flota:

- asignaciones manuales de repartidores;
- tiempos muertos mientras un pedido espera un motorizado;
- repartidores sobrecargados mientras otros permanecen disponibles;
- falta de trazabilidad sobre el estado real de cada entrega;
- dificultad para detectar pedidos con riesgo de retraso;
- poca visibilidad sobre utilización y desempeño de la flota;
- reasignaciones improvisadas ante rechazo, desconexión o indisponibilidad de un repartidor.

FleetBite propone una arquitectura híbrida donde el núcleo transaccional se desarrolla con **Java Spring Boot**, siguiendo principios de **Arquitectura Hexagonal**, mientras que procesos naturalmente asíncronos, como la asignación automática y el monitoreo de SLA, pueden ejecutarse mediante componentes **serverless**.

La aplicación será contenedorizada para garantizar entornos reproducibles de desarrollo y despliegue.

---

## 2. Problema de negocio

En un restaurante con delivery propio pueden coexistir múltiples pedidos en preparación, pedidos listos, repartidores disponibles y repartidores ya realizando entregas.

Cuando la asignación del delivery se realiza de manera manual, el operador debe decidir constantemente:

1. qué pedido debe salir primero;
2. qué motorizado está disponible;
3. qué motorizado se encuentra más cerca o tiene menor carga;
4. si un pedido está cerca de superar su tiempo prometido;
5. qué hacer si un repartidor rechaza o no puede completar la entrega;
6. si conviene reasignar un pedido para evitar retrasos.

Este proceso puede convertirse rápidamente en un cuello de botella durante horas de alta demanda.

El problema central puede resumirse de la siguiente manera:

> **La operación de delivery pierde eficiencia cuando la asignación de pedidos depende de decisiones manuales, no existe priorización basada en tiempo y disponibilidad, y no se cuenta con trazabilidad centralizada del ciclo de entrega.**

---

## 3. Objetivo de la solución

FleetBite busca **reducir el tiempo entre la finalización de preparación de un pedido y su despacho**, automatizando la asignación de la flota motorizada y proporcionando visibilidad sobre el estado de cada entrega.

Los objetivos específicos son:

- centralizar la administración de pedidos y entregas;
- administrar la disponibilidad de repartidores y vehículos;
- asignar automáticamente pedidos a repartidores disponibles;
- priorizar pedidos de acuerdo con su urgencia operativa;
- detectar pedidos en riesgo de incumplir el tiempo de entrega prometido;
- mantener trazabilidad completa de los cambios de estado;
- permitir intervención manual ante excepciones;
- generar indicadores de desempeño de la operación de delivery.

---

## 4. Alcance funcional

El sistema se enfoca en el proceso posterior a la creación del pedido. No pretende implementar inicialmente un ecommerce completo con catálogo, carrito, pagos, promociones o marketplace.

### Incluido en el alcance

- gestión de pedidos;
- gestión de repartidores;
- gestión de vehículos;
- gestión de estados operativos;
- asignación automática de pedidos;
- asignación y reasignación manual;
- seguimiento del ciclo de entrega;
- priorización de pedidos;
- monitoreo de tiempos prometidos;
- historial de eventos y cambios de estado;
- dashboard operativo;
- seguridad y control de acceso por roles;
- procesamiento asíncrono de eventos relevantes;
- virtualización/contenedorización de servicios.

### Fuera del alcance inicial

- pagos en línea;
- marketplace multi-restaurante;
- aplicación móvil completa para clientes;
- promociones y cupones;
- optimización avanzada de rutas con múltiples paradas;
- predicción de demanda con machine learning;
- facturación electrónica;
- integración real con proveedores externos de mapas o pagos.

Estas funcionalidades pueden considerarse evoluciones futuras.

---

## 5. Actores del sistema

### 5.1 Administrador

Responsable de la configuración general del sistema.

Puede:

- administrar usuarios;
- administrar repartidores;
- administrar vehículos;
- configurar parámetros de operación;
- consultar métricas generales.

### 5.2 Operador del restaurante

Supervisa pedidos y su preparación.

Puede:

- registrar o consultar pedidos;
- actualizar estados de preparación;
- marcar un pedido como listo para delivery;
- visualizar el estado de las entregas.

### 5.3 Dispatcher / Coordinador de delivery

Responsable de supervisar la flota y resolver excepciones.

Puede:

- consultar repartidores disponibles;
- revisar asignaciones automáticas;
- reasignar pedidos;
- identificar pedidos retrasados;
- intervenir en pedidos sin repartidor;
- visualizar métricas de operación.

### 5.4 Repartidor

Responsable de ejecutar la entrega.

Puede:

- visualizar su pedido asignado;
- aceptar o rechazar una asignación;
- marcar un pedido como recogido;
- marcar inicio de reparto;
- confirmar entrega;
- reportar una entrega fallida.

---

## 6. Proceso principal de negocio

El flujo principal es:

```text
Pedido registrado
      ↓
CONFIRMED
      ↓
PREPARING
      ↓
READY
      ↓
Evento ORDER_READY
      ↓
Motor de asignación
      ↓
¿Hay repartidor disponible?
      ├── Sí → ASSIGNED
      │          ↓
      │       PICKED_UP
      │          ↓
      │       IN_TRANSIT
      │          ↓
      │       DELIVERED
      │
      └── No → WAITING_FOR_DRIVER
                 ↓
             Reintento / asignación manual
```

El evento más importante del flujo es el cambio del pedido a **READY**, porque representa el momento en que el pedido deja de ser un problema de cocina y pasa a ser un problema logístico.

---

## 7. Estados principales del pedido

Se propone manejar explícitamente el ciclo de vida del pedido mediante estados controlados.

```text
CREATED
CONFIRMED
PREPARING
READY
WAITING_FOR_DRIVER
ASSIGNED
PICKED_UP
IN_TRANSIT
DELIVERED
CANCELLED
FAILED_DELIVERY
```

No todas las transiciones serán válidas.

Ejemplos:

```text
PREPARING → READY               Válido
READY → ASSIGNED                Válido
ASSIGNED → PICKED_UP            Válido
IN_TRANSIT → DELIVERED          Válido
DELIVERED → PREPARING           Inválido
CANCELLED → IN_TRANSIT          Inválido
```

Las reglas de transición deben ser gestionadas por el dominio y no únicamente por la interfaz.

---

## 8. Entidades principales

### 8.1 Order

Representa un pedido que debe ser preparado y entregado.

Información conceptual:

- identificador;
- cliente;
- dirección;
- coordenadas de entrega;
- monto total;
- prioridad;
- estado;
- fecha de creación;
- fecha de preparación;
- fecha de asignación;
- fecha de despacho;
- fecha de entrega;
- tiempo prometido de entrega.

### 8.2 Driver

Representa un repartidor.

Información conceptual:

- identificador;
- nombre;
- teléfono;
- estado operativo;
- ubicación actual;
- vehículo asignado;
- cantidad de pedidos activos.

Estados sugeridos:

```text
AVAILABLE
ASSIGNED
DELIVERING
OFFLINE
```

### 8.3 Vehicle

Representa el vehículo utilizado para delivery.

Información conceptual:

- identificador;
- placa;
- tipo;
- estado;
- repartidor asignado.

### 8.4 DeliveryAssignment

Registra la asignación de un pedido a un repartidor.

Permite mantener trazabilidad incluso cuando existen reasignaciones.

Información conceptual:

- pedido;
- repartidor;
- fecha de asignación;
- tipo de asignación;
- score calculado;
- estado;
- motivo de rechazo o reasignación.

### 8.5 OrderEvent / DeliveryHistory

Mantiene el historial operativo del pedido.

Ejemplo:

```text
18:32 Pedido marcado como READY
18:32 Evento ORDER_READY generado
18:33 Pedido asignado a Driver #05
18:39 Pedido recogido
18:40 Delivery iniciado
19:04 Pedido entregado
```

---

## 9. CRUD dentro del proceso

Aunque el examen solicita un mantenimiento CRUD, el CRUD no será tratado como una funcionalidad aislada.

Servirá para administrar las entidades necesarias para ejecutar el proceso de delivery.

### Pedidos

- Create: registrar pedido.
- Read: consultar listado y detalle.
- Update: actualizar información permitida y estados válidos.
- Delete: restringido a determinados estados, preferentemente antes de iniciar el proceso operativo.

### Repartidores

- Create: registrar repartidor.
- Read: consultar información y estado.
- Update: actualizar disponibilidad y datos.
- Delete / Disable: desactivar repartidor cuando no tenga entregas activas.

### Vehículos

- Create: registrar vehículo.
- Read: consultar flota.
- Update: modificar estado o asignación.
- Delete / Disable: retirar vehículo de operación.

El sistema deberá aplicar reglas de negocio para impedir operaciones CRUD que comprometan la integridad del proceso.

---

## 10. Motor de asignación de repartidores

Cuando un pedido alcanza el estado `READY`, el sistema debe intentar encontrar el repartidor más conveniente.

Para el MVP se utilizará un algoritmo determinístico y explicable.

Factores considerados:

- disponibilidad;
- distancia al punto de recojo;
- cantidad de pedidos activos;
- prioridad del pedido;
- tiempo restante antes de incumplir el SLA.

Ejemplo conceptual:

```text
Driver A
Distancia: 0.8 km
Pedidos activos: 0

Driver B
Distancia: 1.5 km
Pedidos activos: 1

Driver C
Distancia: 2.7 km
Pedidos activos: 0

Resultado recomendado: Driver A
```

Puede utilizarse un score simple:

```text
assignmentScore =
(distanceWeight × distance)
+
(loadWeight × activeOrders)
+
(slaWeight × slaRisk)
```

El candidato con mejor puntaje recibe la asignación.

El objetivo no es crear un algoritmo de optimización matemática complejo, sino demostrar una decisión automática de negocio consistente y defendible.

---

## 11. Priorización de pedidos

Los pedidos pueden manejar niveles de prioridad:

```text
NORMAL
HIGH
CRITICAL
```

La prioridad puede cambiar automáticamente según condiciones operativas.

Por ejemplo:

```text
Pedido A
Tiempo restante: 35 minutos
Prioridad: NORMAL

Pedido B
Tiempo restante: 8 minutos
Prioridad: HIGH
```

El sistema debería favorecer la asignación del segundo pedido cuando exista riesgo real de incumplimiento.

---

## 12. SLA de entrega

Cada pedido tendrá un tiempo de entrega comprometido.

Ejemplo:

```text
Pedido creado: 18:00
Tiempo prometido: 45 minutos
Deadline: 18:45
```

El sistema podrá clasificar el estado del SLA:

```text
ON_TIME
AT_RISK
BREACHED
```

Por ejemplo:

```text
18:20 → ON_TIME
18:37 → AT_RISK
18:46 → BREACHED
```

Esta información alimentará tanto la priorización como el dashboard operativo.

---

## 13. Manejo de excepciones

Un sistema operativo real debe contemplar situaciones distintas al flujo ideal.

### No existe repartidor disponible

```text
READY
↓
WAITING_FOR_DRIVER
↓
Reintento posterior o intervención manual
```

### Repartidor rechaza asignación

```text
ASSIGNED
↓
REJECTED_BY_DRIVER
↓
Nuevo evento de asignación
↓
Otro repartidor
```

### Repartidor queda fuera de servicio

Si tiene un pedido activo:

```text
DRIVER_OFFLINE
↓
Pedido requiere reasignación
↓
REASSIGNMENT_REQUIRED
```

### Entrega fallida

Ejemplos:

- cliente no disponible;
- dirección incorrecta;
- incidente del vehículo;
- pedido dañado.

El pedido cambia a:

```text
FAILED_DELIVERY
```

con registro obligatorio de motivo.

---

## 14. Arquitectura general propuesta

La solución utiliza una arquitectura híbrida.

```text
┌─────────────────────────────┐
│ Frontend Web                │
│ React o Angular             │
└──────────────┬──────────────┘
               │ REST API
               ▼
┌─────────────────────────────┐
│ Spring Boot                 │
│ Arquitectura Hexagonal      │
│                             │
│ Dominio                     │
│ Casos de uso                │
│ Puertos                     │
│ Adaptadores                 │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ PostgreSQL                  │
└─────────────────────────────┘

          EVENTOS
             │
             ▼
┌─────────────────────────────┐
│ EventBridge / SQS           │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ AWS Lambda                  │
│ Assignment Processor        │
└─────────────────────────────┘
```

La arquitectura detallada del backend será documentada en un archivo independiente.

---

## 15. Backend

### Tecnología base

```text
Java
Spring Boot
Spring Data JPA
Spring Security
PostgreSQL
```

### Enfoque arquitectónico

Se propone utilizar **Arquitectura Hexagonal (Ports and Adapters)**.

El objetivo será mantener el dominio independiente de detalles tecnológicos como:

- persistencia;
- REST;
- AWS;
- mensajería;
- autenticación.

Conceptualmente:

```text
Adapters In
REST Controllers
Event Consumers
      ↓
Application
Use Cases
      ↓
Domain
Entities + Business Rules
      ↓
Ports
      ↓
Adapters Out
PostgreSQL
AWS
Notifications
```

El diseño detallado de paquetes, puertos, casos de uso, adaptadores, eventos de dominio, seguridad y persistencia se definirá posteriormente en:

```text
BACKEND.md
```

---

## 16. Frontend

El frontend aún no queda fijado entre **React** y **Angular**.

Ambas alternativas cumplen con el stack solicitado y pueden proporcionar una experiencia rápida si se implementan correctamente.

La decisión deberá considerar principalmente:

- velocidad de implementación;
- experiencia previa;
- facilidad para estructurar módulos;
- rendimiento percibido;
- manejo de estados;
- facilidad para desarrollar dashboards;
- disponibilidad de componentes UI.

### Experiencia de carga

Se implementarán **Skeleton Loaders** o **Skeleton Screens**, que es el nombre de la interfaz mostrada en la referencia visual.

En lugar de mostrar únicamente un spinner mientras una pantalla obtiene información, se representa temporalmente la estructura aproximada del contenido.

Ejemplo:

```text
┌───────────────────────────────┐
│ ██████      ████      ████    │
├───────────────────────────────┤
│ ███   ███████████   ████      │
│ ███   ███████████   ████      │
│ ███   ███████████   ████      │
└───────────────────────────────┘
```

Este patrón mejora el **rendimiento percibido** y evita pantallas vacías durante la carga.

Se utilizarán skeletons principalmente en:

- tabla de pedidos;
- dashboard;
- listado de repartidores;
- detalle de pedido;
- tarjetas de métricas;
- vistas de historial.

La arquitectura, framework elegido, estrategia de estado, componentes y UX se documentarán posteriormente en:

```text
FRONTEND.md
```

---

## 17. Serverless

Serverless será utilizado únicamente donde exista una justificación arquitectónica.

No se pretende convertir todo el backend Spring Boot en funciones Lambda.

### Caso principal: asignación automática

Cuando un pedido cambia a `READY`:

```text
Spring Boot
↓
ORDER_READY
↓
EventBridge / SQS
↓
AWS Lambda
↓
Assignment Processor
↓
Driver seleccionado
```

La función puede:

1. recibir el evento;
2. obtener candidatos disponibles;
3. evaluar los candidatos;
4. seleccionar el mejor;
5. solicitar/registrar la asignación;
6. emitir un nuevo evento.

### Caso secundario: monitoreo de SLA

```text
EventBridge Scheduler
↓
Lambda SLA Monitor
↓
Evaluar pedidos activos
↓
AT_RISK / BREACHED
```

Este proceso puede ejecutarse periódicamente sin mantener un servidor exclusivo para esa tarea.

### Beneficios

- procesamiento desacoplado;
- escalabilidad frente a picos de pedidos;
- ejecución bajo demanda;
- aislamiento de tareas asíncronas;
- menor dependencia entre módulos.

El diseño serverless será descrito con mayor detalle en un documento posterior si la implementación lo requiere.

---

## 18. Virtualización y contenedores

Se utilizará **Docker** para garantizar que los diferentes componentes puedan ejecutarse de manera consistente.

Entorno local propuesto:

```text
Docker Compose
│
├── frontend
├── backend
├── postgres
└── driver-simulator
```

### Driver Simulator

El simulador será un servicio opcional encargado de generar movimientos o cambios de estado de repartidores.

Ejemplo de evento:

```json
{
  "driverId": 5,
  "latitude": -12.1049,
  "longitude": -77.0365,
  "status": "AVAILABLE"
}
```

Esto permite demostrar el proceso sin requerir una aplicación móvil real ni dispositivos GPS.

### Beneficios de la contenedorización

- entorno reproducible;
- instalación más sencilla;
- aislamiento de dependencias;
- reducción de diferencias entre máquinas;
- facilidad de demostración;
- preparación para despliegues futuros.

---

## 19. Seguridad

Se propone implementar autenticación y autorización con Spring Security.

Roles iniciales:

```text
ADMIN
RESTAURANT_OPERATOR
DISPATCHER
DRIVER
```

Las operaciones estarán protegidas de acuerdo con la responsabilidad de cada rol.

Ejemplos:

- un repartidor no debe administrar usuarios;
- un operador no debe modificar configuraciones de seguridad;
- un dispatcher puede reasignar pedidos;
- un administrador puede gestionar flota y usuarios.

El mecanismo concreto de autenticación se definirá en el diseño del backend.

---

## 20. Dashboard operativo

El dashboard debe responder preguntas operativas y no limitarse a mostrar gráficos decorativos.

Indicadores iniciales:

```text
Pedidos activos
Pedidos esperando repartidor
Pedidos en tránsito
Pedidos en riesgo de SLA
Pedidos fuera de SLA
Repartidores disponibles
Repartidores ocupados
```

Indicadores de desempeño:

```text
Tiempo promedio de asignación
Tiempo promedio de entrega
Porcentaje de entregas dentro de SLA
Tasa de entregas fallidas
Utilización de flota
```

Estos indicadores permitirían analizar posteriormente mejoras en el proceso.

---

## 21. Requisitos no funcionales principales

### Rendimiento

- las operaciones CRUD deben responder rápidamente;
- las vistas utilizarán carga progresiva y skeleton loaders;
- los procesos pesados o asíncronos deben desacoplarse del request principal.

### Disponibilidad

Los errores en el proceso serverless no deben impedir que el operador pueda continuar utilizando el sistema principal.

### Trazabilidad

Los cambios relevantes deben registrar:

- usuario;
- fecha;
- estado anterior;
- estado nuevo;
- causa o comentario cuando corresponda.

### Escalabilidad

Los componentes de asignación y monitoreo deberán poder procesar incrementos de eventos sin requerir cambios importantes en el backend principal.

### Mantenibilidad

La Arquitectura Hexagonal deberá mantener las reglas del dominio independientes de frameworks y servicios externos.

### Usabilidad

La interfaz priorizará:

- estados visualmente identificables;
- acciones rápidas;
- tiempos de carga percibidos bajos;
- skeleton screens;
- manejo explícito de errores;
- diseño responsive.

---

## 22. MVP

El MVP debe demostrar correctamente el proceso central antes de agregar funciones secundarias.

### Obligatorio

1. CRUD de pedidos.
2. CRUD de repartidores.
3. CRUD de vehículos.
4. Estados controlados del pedido.
5. Flujo `PREPARING → READY → ASSIGNED → IN_TRANSIT → DELIVERED`.
6. Asignación automática básica.
7. Asignación manual.
8. Registro del historial del pedido.
9. Spring Boot con Arquitectura Hexagonal.
10. PostgreSQL.
11. Frontend React o Angular.
12. Skeleton loaders.
13. Spring Security.
14. Docker Compose.

### Valor agregado prioritario

15. Publicación de `ORDER_READY`.
16. Procesamiento serverless de asignación.
17. SLA de pedidos.
18. Dashboard operativo.
19. Reasignación por rechazo.
20. Simulador de repartidores.

### Solo si existe tiempo suficiente

21. SQS.
22. Reintentos automáticos.
23. Notificaciones.
24. Mapa.
25. Agrupación de pedidos por zonas.
26. Métricas avanzadas.

---

## 23. Criterios de éxito del caso práctico

La solución puede considerarse satisfactoria cuando sea posible demostrar el siguiente escenario de extremo a extremo:

1. Crear un pedido.
2. Prepararlo.
3. Marcarlo como `READY`.
4. Generar el evento de asignación.
5. Seleccionar automáticamente un repartidor disponible.
6. Registrar la asignación.
7. Cambiar el repartidor a ocupado.
8. Recoger el pedido.
9. Iniciar la entrega.
10. Confirmar la entrega.
11. Consultar el historial completo.
12. Visualizar el resultado en el dashboard.

Un segundo escenario debería demostrar una excepción:

1. Pedido listo.
2. No existe repartidor disponible o el repartidor rechaza.
3. Pedido pasa a espera o requiere reasignación.
4. El sistema procesa una nueva asignación.
5. La trazabilidad conserva ambos intentos.

---

## 24. Posibles evoluciones

Una vez consolidado el MVP, FleetBite podría evolucionar hacia:

- optimización de rutas;
- agrupación inteligente de pedidos;
- geofencing;
- integración con Google Maps / Mapbox;
- tracking en tiempo real;
- aplicación móvil para repartidores;
- ETA dinámico;
- análisis histórico de demanda;
- predicción de tiempos de entrega;
- múltiples restaurantes o sedes;
- integración con sistemas POS;
- notificaciones push;
- integración con plataformas externas de delivery.

Estas extensiones no son necesarias para demostrar el valor central del caso práctico.

---

## 25. Stack tecnológico preliminar

| Capa | Tecnología propuesta |
|---|---|
| Frontend | React o Angular |
| Backend | Java + Spring Boot |
| Arquitectura backend | Hexagonal / Ports and Adapters |
| Persistencia | PostgreSQL |
| ORM | Spring Data JPA |
| Seguridad | Spring Security |
| API | REST |
| Eventos | EventBridge y/o SQS |
| Serverless | AWS Lambda |
| Virtualización | Docker + Docker Compose |
| Servidor HTTP / Proxy | Apache o Nginx, según despliegue |
| Documentación API | OpenAPI / Swagger |

---

## 26. Estructura documental propuesta

Este documento funciona como definición general del proyecto.

La documentación se dividirá posteriormente en archivos especializados:

```text
/docs
│
├── GENERAL.md
├── BACKEND.md
├── FRONTEND.md
├── SERVERLESS.md        # si se requiere mayor detalle
├── DATABASE.md          # si el modelo crece lo suficiente
├── DEPLOYMENT.md        # Docker / AWS / ejecución
└── API.md               # opcional, si Swagger no es suficiente
```

### `GENERAL.md`

Visión del sistema, problema, alcance, proceso, arquitectura y decisiones generales.

### `BACKEND.md`

Arquitectura Hexagonal, paquetes, dominio, casos de uso, puertos, adaptadores, persistencia, seguridad, eventos, manejo de errores y testing.

### `FRONTEND.md`

Framework seleccionado, estructura del proyecto, rutas, estado, componentes, Skeleton UI, estrategia de consumo de API, UX y manejo de errores.

### `SERVERLESS.md`

Eventos, Lambda, SQS/EventBridge, idempotencia, retries y observabilidad, únicamente si esta parte alcanza suficiente complejidad para justificar un documento independiente.

---

## 27. Resumen de la propuesta

FleetBite no pretende resolver únicamente el registro de pedidos.

La propuesta modela un proceso operativo completo:

```text
PEDIDO
  ↓
PREPARACIÓN
  ↓
DISPONIBILIDAD
  ↓
ASIGNACIÓN
  ↓
DESPACHO
  ↓
ENTREGA
  ↓
TRAZABILIDAD
```

El CRUD proporciona los datos y controles necesarios, mientras que las reglas de dominio permiten gestionar la operación.

La combinación de **Spring Boot con Arquitectura Hexagonal**, un frontend moderno con **Skeleton Screens**, **PostgreSQL**, **Docker** y procesamiento **serverless orientado a eventos** permite demostrar no solo implementación técnica, sino también diseño de software, arquitectura, lógica de negocio y una solución aplicable a una necesidad empresarial real.
