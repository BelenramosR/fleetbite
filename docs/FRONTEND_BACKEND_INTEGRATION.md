# Seguimiento de integración frontend-backend

## Objetivo

Registrar qué pantallas de FleetBite consumen datos reales del backend, cuáles continúan
utilizando mocks y qué falta para completar cada flujo. Este documento debe actualizarse al
finalizar cada incremento de integración.

## Convenciones

- **Pendiente:** la pantalla utiliza datos locales o todavía no existe integración.
- **Parcial:** parte del flujo consume el backend, pero aún quedan mocks o acciones locales.
- **Conectada:** lectura y acciones principales consumen endpoints reales.
- **Verificada:** además de estar conectada, fue probada contra el backend ejecutándose.
- Todas las respuestas JSON usan el envelope `ApiResponse<T>`.
- Las peticiones protegidas envían `Authorization: Bearer <accessToken>`.
- En desarrollo, Vite reenvía `/api` a `http://localhost:8080`.
- No se considera una pantalla terminada mientras mantenga mocks en su flujo principal.

## Estado general

| Área | Pantalla o flujo | Estado | Fuente actual | Próximo paso |
|---|---|---:|---|---|
| Autenticación | Login | Conectada | Backend | Verificar desde navegador tras reiniciar el backend |
| Autenticación | Restauración de sesión | Conectada | JWT/localStorage | Verificar expiración y renovación desde navegador |
| Autenticación | Logout | Conectada | Backend | Verificar revocación del refresh token |
| Driver | Asignación activa | Conectada | Backend | Verificar el flujo completo en navegador |
| Driver | Perfil | Conectada | Backend | Verificar datos del driver autenticado |
| Driver | Ubicación/disponibilidad | Conectada | Backend | Verificar cambios y persistencia en navegador |
| Pedidos | Lista de pedidos | Conectada | Backend | Verificar filtros con datos persistidos |
| Pedidos | Detalle e historial | Conectada | Backend | Verificar transiciones por rol |
| Pedidos | Nueva orden | Conectada | Backend + selector de mapa | Verificar selección del pin en navegador |
| Despacho | Dashboard | Conectada | Backend | Verificar métricas y navegación en navegador |
| Despacho | Drivers | Conectada | Backend | Verificar lista, detalle y mapa en navegador |
| Flota | Vehículos | Conectada | Backend | Verificar CRUD, ciclo de vida y asignación |
| Identidad | Usuarios | Conectada | Backend | Verificar creación DRIVER con teléfono |
| Reportes | Reportes | Pendiente | Mock local | Conectar al final; evitar endpoints prematuros |
| Configuración | Settings | Pendiente | Visual/local | Determinar si requiere persistencia real |

## Fase 1 — Autenticación y sesión

Estado: **implementada; pendiente de verificación manual en navegador con el backend reiniciado**.

### Integraciones realizadas

| Acción | Endpoint | Autenticación | Resultado |
|---|---|---:|---|
| Iniciar sesión | `POST /api/v1/auth/login` | No | Guarda access y refresh token |
| Renovar sesión | `POST /api/v1/auth/refresh` | No | Reemplaza ambos tokens y reintenta la petición |
| Cerrar sesión | `POST /api/v1/auth/logout` | No | Revoca refresh token y limpia la sesión local |
| Resolver driver | `GET /api/v1/drivers/me` | Sí, DRIVER | Obtiene el UUID real del perfil del driver |

### Cambios del frontend

- El login dejó de consultar `mockUsers`.
- Se agregó un cliente HTTP basado en `fetch`.
- El cliente interpreta `ApiResponse<T>` y convierte errores en `ApiClientError`.
- El access token se adjunta automáticamente.
- Una respuesta `401` intenta renovar la sesión una sola vez.
- La sesión se restaura después de recargar la página.
- La navegación inicial continúa determinada por el rol incluido en el JWT.
- El build de producción fue validado correctamente.

### Pendientes conocidos

- Reiniciar/reconstruir el backend antes de la prueba en navegador; el proceso que estaba
  ejecutándose durante la verificación aún correspondía a una versión anterior.
- El JWT no contiene `fullName`; temporalmente la interfaz deriva un nombre corto del correo.
  Se evaluará más adelante si conviene un endpoint de usuario autenticado o enriquecer el
  resultado del login, sin duplicar información innecesaria.
- Los módulos funcionales posteriores al login todavía usan mocks.

## Fase 2 — Aplicación del driver

Estado: **implementada; pendiente de verificación integral en navegador**.

### Alcance previsto

1. Cargar perfil real mediante `GET /api/v1/drivers/me`.
2. Cambiar disponibilidad con:
   - `POST /api/v1/drivers/me/online`
   - `POST /api/v1/drivers/me/offline`
3. Actualizar ubicación con `PATCH /api/v1/drivers/me/location`.
4. Consultar cada 5–10 segundos `GET /api/v1/driver/assignments/active`.
5. Interpretar `404 RESOURCE_NOT_FOUND` como “sin asignación”, no como error visual.
6. Mostrar una notificación cuando aparezca una asignación nueva en estado `PENDING`.
7. Conectar las acciones:
   - aceptar;
   - rechazar;
   - confirmar recogida;
   - iniciar entrega;
   - completar entrega.
8. Eliminar del flujo principal del driver los mocks de asignaciones y perfil.
9. Consultar el resumen propio mediante `GET /api/v1/driver/assignments/summary`.

### Implementación realizada

- El perfil, vehículo, disponibilidad y ubicación provienen de `/drivers/me`.
- El polling consulta cada 8 segundos la asignación del usuario autenticado.
- `404 RESOURCE_NOT_FOUND` se interpreta como una cola vacía.
- Una asignación `PENDING` nueva abre el aviso, marca la campana y reproduce un sonido breve.
- Aceptar, rechazar, recoger, iniciar y completar invocan los endpoints protegidos reales.
- La respuesta de asignación activa incluye un resumen de su propia orden; no se habilitó al
  driver el acceso al listado o detalle general de órdenes.
- El SLA mostrado se deriva de `promisedDeliveryAt` entregado por el backend.
- Las tarjetas de entregas completadas hoy y porcentaje de aceptación consumen
  `/driver/assignments/summary`. El driver se resuelve desde el JWT y no desde un ID enviado
  por el navegador, evitando consultar estadísticas de otro repartidor.
- “Reportar entrega fallida” permanece deshabilitado a nivel de integración porque no forma
  parte de los endpoints de autoservicio implementados en esta fase.

### Criterios para dar la fase por verificada

- Un driver inicia sesión y solo observa su perfil.
- Puede cambiar su disponibilidad y actualizar su ubicación.
- Una asignación automática aparece sin recargar la página.
- Puede aceptarla o rechazarla.
- Puede recorrer las transiciones permitidas hasta completar la entrega.
- Una asignación ajena produce `403 ACCESS_DENIED` y no altera el estado visual local.
- Al recargar la página se conserva la sesión y se recupera la asignación activa.

## Orden de las fases siguientes

1. Aplicación del driver.
2. Lista, creación y detalle de pedidos.
3. Despacho y gestión de drivers.
4. Vehículos y asignación de flota.
5. Administración de usuarios.
6. Dashboard y reportes.
7. Limpieza final de mocks, pruebas end-to-end y configuración cloud.

## Fase 3 — Pedidos

Estado: **implementada; pendiente de verificación integral en navegador**.

### Implementación realizada

- El listado consume `GET /api/v1/orders` y conserva filtros, búsqueda y SLA calculado desde
  `promisedDeliveryAt`.
- El detalle consume `GET /api/v1/orders/{id}`.
- El historial consume `GET /api/v1/orders/{id}/history`; ya no utiliza `mockTimeline`.
- La creación consume `POST /api/v1/orders`; la promesa y prioridad no se inventan en el
  frontend, sino que quedan bajo control del backend.
- Las transiciones de restaurante consumen `confirm`, `start-preparation` y `ready`, y el
  historial se recarga después de cada cambio.
- Al crear un pedido, el listado vuelve a consultar el backend aunque la pantalla ya estuviera
  montada.

### Pendientes conscientes

- La creación incluye un selector Leaflet/OpenStreetMap: el usuario puede hacer clic o
  arrastrar el pin y abrir esas coordenadas en Google Maps. El texto de la dirección continúa
  siendo editable porque la conversión inversa de coordenadas a dirección requeriría un
  servicio de geocodificación.
- La asignación automática consume `POST /api/v1/orders/{id}/auto-assign`.
- La asignación manual carga `GET /api/v1/drivers` y crea la asignación mediante
  `POST /api/v1/orders/{id}/assign`; el panel ya no utiliza drivers mock.
- El backend no registra aún el usuario que ejecutó cada evento; el historial muestra
  “Sistema” como actor neutral.

## Fase 4 — Motorizados de despacho

Estado: **implementada; pendiente de verificación integral en navegador**.

- La lista y el detalle consumen `GET /api/v1/drivers`.
- La asignación y pedido activo se obtienen combinando `GET /api/v1/assignments` y
  `GET /api/v1/orders`.
- El mapa muestra únicamente coordenadas persistidas por cada driver.
- Los filtros disponible, ocupado y fuera de línea se calculan con estados reales.
- “Activar” y “Desactivar” consumen `POST /api/v1/drivers/{id}/online` y
  `POST /api/v1/drivers/{id}/offline` y luego recargan la información.
- “Entregas hoy” cuenta asignaciones completadas durante el día de Lima.
- La próxima pantalla será el Dashboard de despacho.

## Registro de verificaciones

## Fase 5 — Dashboard operativo y actualización dinámica

Estado: **implementada; pendiente de verificación integral en navegador**.

- El Dashboard combina `GET /api/v1/orders`, `GET /api/v1/assignments` y
  `GET /api/v1/drivers`; se eliminaron `mockMetrics` y `mockOrders` de su flujo.
- Pedidos activos, sin repartidor, SLA, disponibilidad, promedios y cola rápida se calculan
  con información persistida.
- Dashboard, lista de pedidos y motorizados se actualizan cada 15 segundos.
- El detalle del pedido se actualiza cada 10 segundos para reflejar acciones realizadas desde
  la sesión del driver.
- La aplicación del driver conserva polling cada 8 segundos.
- Las acciones muestran toast verde con check al completarse y rojo con X al fallar.

## Fase 6 — Administración de usuarios y flota

Estado: **implementada; pendiente de verificación integral en navegador**.

- Usuarios consume `GET/POST/PUT /api/v1/users` y los endpoints `activate/deactivate`.
- Al crear un DRIVER se exige teléfono: se provisiona el perfil al crear el usuario y luego se
  actualiza mediante `PUT /api/v1/drivers/{id}`.
- El rol queda bloqueado durante la edición porque el backend solo provisiona perfiles DRIVER
  durante la creación.
- Usuarios se actualiza cada 15 segundos y utiliza toast verde/rojo según el resultado real.
- Flota consume el CRUD de `/api/v1/vehicles`, las transiciones de ciclo de vida y el vínculo
  mediante `/api/v1/drivers/{id}/vehicle`.
- El formulario de vehículo solicita placa y tipo, los atributos existentes en el dominio.
- Flota combina vehículos y drivers reales y se actualiza cada 15 segundos.

| Fecha | Fase | Verificación | Resultado |
|---|---|---|---|
| 2026-08-10 | Autenticación | `pnpm build` | Correcto; 2441 módulos transformados |
| 2026-08-10 | Backend previo | Suite Java/Testcontainers | 312 pruebas registradas sin fallos |
| 2026-08-10 | Driver | Build de producción del frontend | Correcto; 2442 módulos transformados |
| 2026-08-10 | Driver | Servicio y reglas de arquitectura backend | Correcto |
| 2026-08-10 | Pedidos | Build de producción del frontend | Correcto; 2443 módulos transformados |
| 2026-08-10 | Pedidos | Historial, asignación pendiente y selector de mapa | Build correcto; 2444 módulos |
| 2026-08-10 | Driver | Asignación real de Driver 2 después de reconstruir backend | `PENDING` y orden enriquecida verificadas |
| 2026-08-10 | Pedidos | Detalle de estado terminal | SLA oculto para entregado/cancelado/fallido |
| 2026-08-10 | Driver | Confirmación de entrega con driver previamente disponible | Corregida, probada y desplegada localmente |
| 2026-08-10 | Motorizados | Build de producción del frontend | Correcto; 2444 módulos transformados |
| 2026-08-10 | Dashboard y polling | Build de producción del frontend | Correcto; 2445 módulos transformados |
| 2026-08-10 | Usuarios y Flota | Build de producción del frontend | Correcto; 2446 módulos transformados |

## Regla de actualización

Al completar una pantalla se debe:

1. cambiar su estado en la matriz general;
2. registrar los endpoints efectivamente consumidos;
3. indicar qué mocks fueron eliminados o conservados;
4. anotar las pruebas ejecutadas;
5. registrar cualquier deuda técnica consciente;
6. señalar la siguiente pantalla que se conectará.
