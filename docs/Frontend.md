# FleetBite - Diseño de Frontend

## 1. Objetivo del frontend

El frontend de FleetBite debe permitir operar de forma rápida y clara el ciclo completo de pedidos con delivery propio: recepción, preparación, asignación de motorizados, seguimiento de entregas y resolución de excepciones operativas.

La prioridad no es construir una interfaz visualmente recargada, sino una aplicación operativa con buena respuesta, baja fricción y suficiente visibilidad para que un operador pueda detectar qué pedido requiere atención en pocos segundos.

La interfaz debe priorizar:

- rapidez de carga;
- navegación simple;
- estados visibles;
- reducción de clics;
- feedback inmediato;
- vistas adaptadas por rol;
- tratamiento claro de errores;
- loaders tipo skeleton para evitar saltos visuales;
- componentes reutilizables;
- separación limpia entre lógica de presentación y acceso a datos.

---

## 2. Framework propuesto

Para este proyecto, la recomendación principal es **React con Vite y TypeScript**.

Angular también sería una alternativa válida, pero para un examen práctico de alcance limitado React ofrece una relación favorable entre velocidad de implementación, flexibilidad y simplicidad estructural.

### React + Vite + TypeScript

Ventajas para FleetBite:

- tiempos de inicio y compilación muy rápidos con Vite;
- ecosistema maduro;
- composición sencilla de componentes;
- control fino sobre carga diferida;
- buena integración con TanStack Query;
- implementación directa de skeleton loaders;
- menor cantidad de boilerplate para un proyecto de este tamaño;
- fácil separación por features;
- buen soporte para dashboards y vistas operativas.

### Angular

Angular sería recomendable si se quisiera demostrar una estructura empresarial más rígida desde el inicio, con:

- DI integrada;
- routing completo;
- formularios reactivos;
- interceptores;
- guards;
- servicios estructurados;
- convenciones fuertes.

Sin embargo, para este proyecto específico, React permite llegar más rápido a un resultado funcional sin sacrificar mantenibilidad.

### Decisión recomendada

```text
Frontend recomendado:
React
TypeScript
Vite
React Router
TanStack Query
Axios
React Hook Form
Zod
shadcn/ui o componentes propios
Tailwind CSS
Recharts para gráficos
```

---

## 3. Skeleton UI

El patrón que se quería incorporar se denomina:

- Skeleton Loader
- Skeleton Screen
- Skeleton UI

Su objetivo es mostrar una representación temporal de la estructura de la pantalla mientras los datos están cargando.

En lugar de mostrar únicamente:

```text
Loading...
```

se representa visualmente la estructura esperada.

Ejemplo:

```text
┌──────────────────────────────────────┐
│ ████████████     ██████    ██████   │
├──────────────────────────────────────┤
│ ███   ███████████████   ███   ███   │
│ ███   ███████████████   ███   ███   │
│ ███   ███████████████   ███   ███   │
└──────────────────────────────────────┘
```

Esto mejora la percepción de velocidad y reduce el movimiento brusco del layout.

Debe aplicarse principalmente en:

- dashboard;
- tabla de pedidos;
- tabla de motorizados;
- detalle de pedido;
- tarjetas de métricas;
- historial del pedido;
- panel de asignación;
- gráficos.

No debe utilizarse como decoración permanente. Solo durante estados reales de carga.

---

## 4. Arquitectura del frontend

Se recomienda una arquitectura orientada a features.

En lugar de organizar todo únicamente por tipo técnico:

```text
components/
pages/
services/
hooks/
```

se recomienda agrupar por dominio funcional.

```text
src/
├── app/
│   ├── router/
│   ├── providers/
│   └── layouts/
│
├── features/
│   ├── auth/
│   ├── dashboard/
│   ├── orders/
│   ├── drivers/
│   ├── fleet/
│   ├── dispatch/
│   └── users/
│
├── shared/
│   ├── components/
│   ├── hooks/
│   ├── lib/
│   ├── types/
│   └── constants/
│
├── services/
│   └── api/
│
└── main.tsx
```

Esto permite que cada feature sea más autocontenida.

---

## 5. Estructura propuesta por feature

Ejemplo para pedidos:

```text
features/orders/
├── api/
│   ├── getOrders.ts
│   ├── getOrderById.ts
│   ├── createOrder.ts
│   ├── updateOrder.ts
│   └── updateOrderStatus.ts
│
├── components/
│   ├── OrderTable.tsx
│   ├── OrderStatusBadge.tsx
│   ├── OrderFilters.tsx
│   ├── OrderForm.tsx
│   ├── OrderTimeline.tsx
│   └── OrderSkeleton.tsx
│
├── hooks/
│   ├── useOrders.ts
│   └── useOrder.ts
│
├── pages/
│   ├── OrdersPage.tsx
│   ├── OrderDetailPage.tsx
│   └── OrderCreatePage.tsx
│
├── schemas/
│   └── order.schema.ts
│
├── types/
│   └── order.types.ts
│
└── utils/
    └── order.utils.ts
```

Esta estructura evita que todo termine mezclado en archivos gigantes.

---

## 6. Rutas principales

```text
/login

/dashboard

/orders
/orders/new
/orders/:id

/dispatch

/drivers
/drivers/:id

/fleet

/users
```

Las rutas pueden variar según rol.

---

## 7. Layout principal

La aplicación debe usar un layout operativo consistente.

```text
┌──────────────────────────────────────────────┐
│ Header                                       │
├───────────────┬──────────────────────────────┤
│ Sidebar       │ Content                      │
│               │                              │
│ Dashboard     │                              │
│ Pedidos       │                              │
│ Despacho      │                              │
│ Motorizados   │                              │
│ Flota         │                              │
│ Usuarios      │                              │
└───────────────┴──────────────────────────────┘
```

Debe ser responsivo, aunque la prioridad funcional puede estar en escritorio porque el operador central probablemente trabajará desde una PC.

Para el rol DRIVER sí conviene optimizar específicamente vistas móviles.

---

## 8. Roles y experiencia de usuario

### ADMIN

Puede acceder a:

- usuarios;
- configuración;
- flota;
- motorizados;
- dashboard;
- pedidos.

### RESTAURANT_OPERATOR

Puede acceder a:

- pedidos;
- preparación;
- cambio de estado;
- dashboard operativo.

### DISPATCHER

Puede acceder a:

- pedidos listos;
- asignaciones;
- motorizados;
- excepciones;
- SLA;
- reasignaciones.

### DRIVER

Debe tener una experiencia mucho más simple.

```text
Pedido asignado
↓
Aceptar
↓
Recogido
↓
En camino
↓
Entregado
```

No debería ver módulos administrativos innecesarios.

---

## 9. Dashboard operativo

El dashboard debe responder preguntas concretas.

### KPIs principales

```text
Pedidos activos
Esperando repartidor
En camino
En riesgo SLA
Motorizados disponibles
```

Ejemplo:

```text
┌─────────────────┐
│ PEDIDOS ACTIVOS │
│       18        │
└─────────────────┘

┌─────────────────┐
│ SIN REPARTIDOR  │
│        3        │
└─────────────────┘

┌─────────────────┐
│ EN CAMINO       │
│        9        │
└─────────────────┘

┌─────────────────┐
│ SLA EN RIESGO   │
│        2        │
└─────────────────┘
```

### Gráficos recomendados

Máximo dos o tres.

- pedidos por estado;
- entregas por hora;
- SLA cumplido vs incumplido.

No conviene llenar el dashboard de gráficos irrelevantes. Una interfaz con ocho donuts es una forma sofisticada de ocultar que nadie sabe qué necesita mirar.

---

## 10. Vista de pedidos

La tabla de pedidos debe ser uno de los centros operativos.

Columnas sugeridas:

```text
ID
Cliente
Dirección
Estado
Prioridad
Motorizado
Tiempo restante SLA
Total
Creado
Acciones
```

Filtros:

- estado;
- prioridad;
- motorizado;
- rango de fecha;
- pedidos en riesgo;
- pedidos sin asignación.

Búsqueda por:

- ID;
- cliente;
- dirección.

La tabla debe incluir paginación server-side si el backend la soporta.

---

## 11. Estados visuales

Los estados deben representarse mediante badges consistentes.

Ejemplo conceptual:

```text
CREATED       neutral
PREPARING     warning
READY         attention
ASSIGNED      info
IN_TRANSIT    progress
DELIVERED     success
CANCELLED     danger
FAILED        danger
```

No se recomienda depender solo del color. También debe aparecer el texto del estado.

---

## 12. Detalle del pedido

La vista de detalle debe reunir toda la información del proceso.

```text
Pedido #ORD-2034

Cliente
Dirección
Monto
Prioridad
Estado
SLA

Motorizado asignado

Timeline

18:10 Pedido creado
18:12 Confirmado
18:20 Preparando
18:32 Listo
18:33 Asignado
18:38 Recogido
18:50 En camino
```

También debe permitir las acciones válidas según estado.

Ejemplo:

```text
PREPARING

[Marcar como listo]
```

No mostrar botones que representen acciones imposibles.

---

## 13. Máquina de estados en frontend

El frontend no debe decidir por sí mismo las reglas definitivas del dominio.

El backend sigue siendo la fuente de verdad.

Sin embargo, el frontend puede utilizar las reglas para mejorar UX.

Ejemplo:

```ts
const allowedActions = {
  PREPARING: ['MARK_READY'],
  READY: ['ASSIGN_DRIVER'],
  ASSIGNED: ['PICK_UP'],
  IN_TRANSIT: ['DELIVER'],
};
```

Pero el backend debe validar nuevamente cualquier transición.

---

## 14. Dispatch Board

Esta puede ser una de las mejores pantallas de la demo.

Debe permitir al dispatcher ver simultáneamente pedidos y repartidores.

```text
PEDIDOS LISTOS                    REPARTIDORES

ORD-21                            Carlos
SLA 12 min                        AVAILABLE
Priority HIGH                     0.8 km

ORD-22                            Lucía
SLA 28 min                        AVAILABLE
Priority NORMAL                   1.3 km

ORD-23                            Diego
SLA 8 min                         DELIVERING
Priority CRITICAL
```

La asignación automática debe mostrarse claramente.

Ejemplo:

```text
ORD-21
Assigned automatically → Carlos
```

El dispatcher puede tener opción de reasignar manualmente si su rol lo permite.

---

## 15. Vista de motorizados

Debe mostrar:

```text
Nombre
Estado
Vehículo
Pedido activo
Última ubicación
Última actualización
```

Estados:

```text
AVAILABLE
ASSIGNED
DELIVERING
OFFLINE
```

La vista de detalle puede incluir:

- entregas realizadas;
- estado actual;
- vehículo;
- pedido actual;
- historial básico.

---

## 16. Manejo de datos remotos

Se recomienda **TanStack Query** para manejar estado del servidor.

No usar Redux para todo por simple costumbre.

TanStack Query puede manejar:

- cache;
- loading;
- errores;
- invalidación;
- refetch;
- stale data;
- mutations.

Ejemplo conceptual:

```ts
const { data, isLoading, isError } = useQuery({
  queryKey: ['orders', filters],
  queryFn: () => getOrders(filters),
});
```

Si `isLoading`:

```tsx
return <OrderTableSkeleton />;
```

---

## 17. Axios

Se puede utilizar una instancia centralizada.

```text
services/api/httpClient.ts
```

Responsabilidades:

- base URL;
- headers;
- token;
- interceptores;
- normalización básica de errores.

Flujo:

```text
Component
↓
Feature API function
↓
Axios Client
↓
Spring Boot
```

---

## 18. Autenticación

Flujo recomendado:

```text
/login
↓
email + password
↓
POST /auth/login
↓
JWT
↓
sesión frontend
```

El token debe adjuntarse automáticamente en requests protegidos.

Para un examen práctico puede almacenarse en memoria o mediante la estrategia definida con el backend.

Si se implementan refresh tokens, conviene evaluar cookie HTTP-only para el refresh token.

No se recomienda meter OAuth completo si el backend ya usa autenticación local JWT y el tiempo es limitado.

---

## 19. Route Guards

Debe existir control de acceso por autenticación y rol.

Ejemplo:

```text
ProtectedRoute
RoleRoute
```

Flujo:

```text
Usuario solicita /users
↓
¿Autenticado?
↓
¿ADMIN?
↓
Sí → permitir
No → 403 / redirect
```

Esto mejora UX, aunque el backend debe continuar validando permisos.

---

## 20. Formularios

Se recomienda:

```text
React Hook Form
+
Zod
```

Para:

- creación de pedido;
- edición de motorizado;
- creación de vehículo;
- gestión de usuarios.

Ejemplo:

```ts
const orderSchema = z.object({
  customerName: z.string().min(2),
  customerAddress: z.string().min(5),
  totalAmount: z.number().positive(),
});
```

La validación frontend sirve para UX.

La validación backend continúa siendo obligatoria.

---

## 21. Feedback al usuario

Cada acción debe producir feedback claro.

Ejemplos:

```text
Pedido creado correctamente
```

```text
Motorizado asignado
```

```text
No se pudo actualizar el pedido
```

Se pueden usar toast notifications para acciones puntuales.

No deberían reemplazar mensajes persistentes cuando el error requiere una acción del usuario.

---

## 22. Manejo de errores

Se recomienda distinguir:

### 400

Validación.

Mostrar información cerca del formulario.

### 401

Sesión inválida.

Redirigir al login.

### 403

Usuario sin permisos.

Mostrar vista de acceso denegado.

### 404

Entidad inexistente.

### 409

Conflicto de negocio.

Ejemplo:

```text
El motorizado ya fue asignado a otro pedido.
```

Este caso es especialmente importante porque puede ocurrir por concurrencia.

### 500

Error inesperado.

Mostrar mensaje general y permitir reintentar.

---

## 23. Skeleton components

Se recomienda crear componentes reutilizables.

```text
shared/components/skeleton/
├── TableSkeleton.tsx
├── CardSkeleton.tsx
├── DetailSkeleton.tsx
└── DashboardSkeleton.tsx
```

Ejemplo:

```tsx
function OrdersPage() {
  const { data, isLoading } = useOrders();

  if (isLoading) {
    return <OrderTableSkeleton />;
  }

  return <OrderTable orders={data} />;
}
```

La estructura del skeleton debe aproximarse a la estructura final para reducir layout shifts.

---

## 24. Lazy loading

Para mejorar carga inicial:

```ts
const DashboardPage = lazy(() => import(...));
const OrdersPage = lazy(() => import(...));
const DriversPage = lazy(() => import(...));
```

Con React Router se puede cargar cada ruta bajo demanda.

Esto evita descargar desde el inicio código que el usuario posiblemente no utilizará.

---

## 25. Code splitting

Vite y React pueden separar automáticamente chunks.

Aplicar especialmente a:

- dashboard;
- gráficos;
- módulos administrativos;
- mapas si posteriormente se incorporan.

No conviene microoptimizar veinte kilobytes mientras el backend tarda cuatro segundos en responder. La optimización debe atacar problemas reales.

---

## 26. Optimización de renderizado

Evitar optimizaciones prematuras.

Solo utilizar:

```text
React.memo
useMemo
useCallback
```

cuando exista una razón medible.

La estrategia principal debe ser:

- buen diseño de componentes;
- evitar estados globales innecesarios;
- queries correctamente cacheadas;
- paginación;
- lazy loading;
- evitar requests duplicados.

---

## 27. Polling y actualización operativa

El dashboard y dispatch board necesitan cierta actualización periódica.

Versión MVP:

```text
TanStack Query refetchInterval
```

Ejemplo:

```text
cada 10-15 segundos
```

para pedidos activos.

No es necesario implementar WebSocket desde el inicio.

### Evolución posterior

```text
WebSocket
Server-Sent Events
```

para eventos en tiempo real.

Para el examen, polling controlado es más seguro y suficientemente demostrable.

---

## 28. Estado local vs estado remoto

### Estado remoto

Usar TanStack Query:

- pedidos;
- motorizados;
- usuarios;
- métricas.

### Estado local

Usar `useState` o context solo cuando corresponda:

- filtros temporales;
- modales;
- selección UI;
- preferencias de usuario.

Evitar crear un store global enorme para información que realmente pertenece al servidor.

---

## 29. Componentes compartidos

```text
Button
Input
Select
Dialog
Table
Pagination
StatusBadge
MetricCard
EmptyState
ErrorState
Skeleton
ConfirmDialog
```

La idea es mantener consistencia visual y reducir duplicación.

---

## 30. Tabla reutilizable

Para varias entidades se puede crear una base reusable.

Sin convertirla en un componente genérico incomprensible de 700 líneas.

Debe soportar:

- columnas;
- loading;
- empty state;
- paginación;
- acciones.

---

## 31. Empty states

Un estado vacío no debería mostrar una tabla rota.

Ejemplo:

```text
No hay pedidos esperando asignación.
```

Esto es especialmente útil en:

- pedidos pendientes;
- motorizados disponibles;
- excepciones;
- historial.

---

## 32. Confirmaciones

Acciones críticas deben pedir confirmación.

Ejemplo:

```text
Cancelar pedido
Reasignar motorizado
Eliminar vehículo
Desactivar usuario
```

No es necesario confirmar acciones reversibles o triviales.

---

## 33. Diseño responsive

### Desktop

Prioridad para:

- operadores;
- dispatcher;
- administrador.

### Mobile

Prioridad para:

- driver.

La vista del motorizado debe poder utilizarse cómodamente desde teléfono.

Ejemplo:

```text
Pedido #3042

Cliente
Dirección

[ACEPTAR]

[RECOGIDO]

[ENTREGADO]
```

Botones amplios y pocas acciones visibles simultáneamente.

---

## 34. Accesibilidad básica

Implementar como mínimo:

- labels asociados;
- navegación por teclado;
- focus visible;
- botones semánticos;
- contraste adecuado;
- estados no dependientes solo del color;
- `aria-label` donde sea necesario.

No hace falta convertir el examen en una auditoría WCAG completa, pero sí evitar errores elementales.

---

## 35. TypeScript

Debe utilizarse de forma estricta.

Evitar:

```ts
any
```

como solución universal.

Tipos importantes:

```text
Order
OrderStatus
Driver
DriverStatus
Vehicle
User
PaginatedResponse<T>
ApiError
```

Ejemplo:

```ts
type OrderStatus =
  | 'CREATED'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY'
  | 'ASSIGNED'
  | 'PICKED_UP'
  | 'IN_TRANSIT'
  | 'DELIVERED'
  | 'CANCELLED';
```

---

## 36. Contratos con backend

El frontend debe utilizar exactamente los contratos expuestos por la API.

Idealmente, Swagger/OpenAPI puede servir como referencia.

Si sobra tiempo, se podría generar automáticamente tipos TypeScript desde OpenAPI.

No es necesario para el MVP.

---

## 37. Variables de entorno

Ejemplo:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

No hardcodear URLs en componentes.

---

## 38. Docker

El frontend debe incluir un Dockerfile.

En desarrollo:

```text
Vite dev server
```

En producción puede construirse y servirse desde:

```text
Nginx
```

Aunque la convocatoria mencione Apache como opción, Nginx es totalmente válido si se documenta la decisión.

Arquitectura:

```text
React build
↓
Nginx container
↓
Browser
```

---

## 39. Integración con Docker Compose

```text
frontend
backend
postgres
simulator
```

El frontend consume:

```text
http://backend:8080
```

dentro de la red Docker, o se configura un reverse proxy según entorno.

---

## 40. Tests

No se necesita cobertura obsesiva.

Priorizar:

### Unit/component tests

- validaciones;
- componentes de estados;
- lógica de acciones permitidas.

### Integration UI

- login;
- creación de pedido;
- cambio de estado;
- asignación.

Herramientas posibles:

```text
Vitest
React Testing Library
```

Si sobra tiempo:

```text
Playwright
```

para un flujo end-to-end principal.

---

## 41. Performance

Objetivos generales:

- primera carga rápida;
- evitar bundles innecesariamente grandes;
- evitar requests duplicados;
- usar skeletons;
- cachear consultas;
- lazy loading;
- comprimir build mediante servidor web;
- paginar tablas grandes.

No hace falta prometer tiempos artificiales si no se han medido.

---

## 42. Observabilidad frontend

Para el MVP:

```text
console estructurada solo en desarrollo
```

No dejar `console.log` indiscriminados en producción.

En una evolución:

- Sentry;
- métricas Web Vitals;
- trazabilidad de errores frontend.

---

## 43. Flujo de ejemplo

### Operador

```text
Login
↓
Pedidos
↓
Crear pedido
↓
PREPARING
↓
Marcar READY
```

### Sistema

```text
READY
↓
backend genera evento
↓
serverless asigna motorizado
```

### Frontend

```text
TanStack Query refresca
↓
Pedido aparece ASSIGNED
↓
Se muestra motorizado
```

### Driver

```text
Aceptar
↓
Recogido
↓
IN_TRANSIT
↓
DELIVERED
```

### Dashboard

```text
Actualiza métricas
```

Ese sería el flujo principal de la demo.

---

## 44. MVP de frontend

### Obligatorio

1. Login.
2. Layout principal.
3. Dashboard.
4. Tabla de pedidos.
5. Crear pedido.
6. Detalle de pedido.
7. Cambio de estados.
8. Tabla de motorizados.
9. Dispatch board básico.
10. Skeleton loaders.
11. Manejo de errores.
12. Control de acceso por roles.

### Deseable

13. Gráfico SLA.
14. Filtros avanzados.
15. Vista móvil del driver.
16. Reasignación manual.
17. Toasts.
18. Lazy routes.

### Bonus

19. Mapa.
20. Actualización en tiempo real.
21. Animaciones de transición.
22. PWA.

No implementar bonus antes de tener completamente operativo el flujo principal.

---

## 45. Orden recomendado de implementación

### Fase 1

```text
Vite
TypeScript
Routing
Layout
Axios
```

### Fase 2

```text
Login
JWT
Protected routes
```

### Fase 3

```text
Orders list
Order detail
Create order
Status transitions
```

### Fase 4

```text
Drivers
Dispatch board
```

### Fase 5

```text
Dashboard
Metrics
Charts
```

### Fase 6

```text
Skeletons
Errors
Empty states
Responsive polish
```

### Fase 7

```text
Docker
Testing
Final cleanup
```

---

## 46. Decisiones que deben poder defenderse

### ¿Por qué React?

Porque permite construir rápidamente una aplicación operativa modular, tiene un ecosistema maduro y facilita implementar carga diferida y gestión eficiente del estado remoto.

### ¿Por qué Vite?

Por velocidad de desarrollo y generación sencilla del build.

### ¿Por qué TanStack Query?

Porque la mayor parte del estado relevante proviene del backend. Gestiona cache, refetch, loading y mutations sin introducir un store global innecesario.

### ¿Por qué Skeleton UI?

Porque mantiene estable la estructura visual durante cargas y mejora la percepción de rendimiento.

### ¿Por qué no Redux?

Porque no existe suficiente estado global complejo que lo justifique en el MVP.

### ¿Por qué polling y no WebSocket inicialmente?

Porque reduce complejidad y permite entregar un sistema estable. El diseño puede evolucionar posteriormente a comunicación en tiempo real.

### ¿Por qué feature-based architecture?

Porque mantiene juntas las piezas relacionadas con cada dominio funcional y facilita el crecimiento del sistema.

---

## 47. Resultado esperado

El frontend de FleetBite debe sentirse como una herramienta operativa, no como un ecommerce para clientes finales.

La interfaz debe permitir que un operador pueda responder inmediatamente:

```text
¿Qué pedidos están activos?
¿Qué pedidos están retrasándose?
¿Qué pedidos no tienen repartidor?
¿Qué motorizados están disponibles?
¿Qué pedido requiere atención ahora?
```

Si esas preguntas pueden responderse en pocos segundos, el frontend está cumpliendo su propósito.

