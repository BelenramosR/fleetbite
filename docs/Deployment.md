# FleetBite - Docker, Infraestructura y Despliegue

## 1. Propósito

Este documento define cómo ejecutar y desplegar FleetBite de forma reproducible.

El objetivo de la virtualización/contenedorización es evitar diferencias entre ambientes y facilitar que otra persona pueda levantar el proyecto sin configurar manualmente cada servicio.

---

## 2. Componentes

Arquitectura de ejecución propuesta:

```text
Browser
   |
   v
Nginx / Apache
   |
   +------ Frontend SPA
   |
   v
Spring Boot API
   |
   v
PostgreSQL
```

Servicios adicionales:

```text
AWS / LocalStack
 |
 +-- EventBridge
 +-- SQS
 +-- Lambda
```

---

## 3. Docker

Componentes a contenerizar:

```text
frontend
backend
postgres
```

Opcional:

```text
localstack
driver-simulator
```

---

## 4. Docker Compose

Estructura conceptual:

```yaml
services:
  postgres:
    image: postgres

  backend:
    build: ./backend
    depends_on:
      - postgres

  frontend:
    build: ./frontend
    depends_on:
      - backend

  localstack:
    image: localstack/localstack
```

La configuración final debe utilizar variables de entorno y health checks.

---

## 5. Backend Dockerfile

Estrategia multi-stage:

```text
Stage 1
Maven / Gradle build

Stage 2
JRE runtime
```

Objetivo:

- imagen final más pequeña;
- no incluir herramientas de compilación;
- mejor tiempo de distribución.

Ejemplo conceptual:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
# compile

FROM eclipse-temurin:21-jre
# copy jar
# run
```

---

## 6. Frontend Dockerfile

También multi-stage:

```text
Node
  |
npm build
  |
dist
  |
Nginx
```

El contenedor final solo necesita archivos estáticos y Nginx.

---

## 7. Reverse proxy

Se recomienda Nginx para el frontend.

Ejemplo:

```text
/
  -> React SPA

/api/
  -> Spring Boot
```

Esto permite que el navegador consuma:

```text
/api/orders
```

sin exponer puertos internos innecesariamente.

Apache también es válido por el requisito de la prueba, pero Nginx resulta simple para servir una SPA y actuar como reverse proxy.

---

## 8. Red Docker

Los servicios pueden compartir una red interna:

```text
fleetbite-network
```

Ejemplo:

```text
frontend -> backend:8080
backend -> postgres:5432
```

PostgreSQL no necesita estar expuesto públicamente.

---

## 9. Volúmenes

PostgreSQL:

```text
postgres_data
```

Objetivo:

- conservar datos tras recrear contenedores.

---

## 10. Variables de entorno

Nunca colocar valores sensibles directamente en Git.

Ejemplos:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

JWT_SECRET

AWS_REGION
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY

VITE_API_URL
```

Para desarrollo:

```text
.env
```

Debe incluirse:

```text
.env.example
```

y excluirse el real del repositorio.

---

## 11. Perfiles Spring

Propuesta:

```text
local
docker
prod
test
```

Ejemplo:

```text
application.yml
application-local.yml
application-docker.yml
application-prod.yml
```

No duplicar toda la configuración. Solo sobrescribir diferencias.

---

## 12. Health checks

Backend:

```text
/actuator/health
```

PostgreSQL:

```text
pg_isready
```

Docker Compose debe esperar servicios saludables cuando sea necesario.

---

## 13. Ejecución local

Objetivo ideal:

```bash
docker compose up --build
```

Después:

```text
Frontend:
http://localhost

Backend:
http://localhost/api

Swagger:
http://localhost/api/swagger-ui/index.html
```

Los puertos exactos pueden cambiar.

---

## 14. LocalStack

Si se desea demostrar serverless localmente:

```text
localstack
```

puede simular:

- SQS;
- EventBridge;
- Lambda;
- SNS.

Arquitectura:

```text
Spring Boot
   |
   v
LocalStack
   |
   v
SQS / Lambda
```

Esto permite una demo sin depender de conectividad o credenciales reales.

---

## 15. AWS para demo

Si se despliega una parte:

```text
API / Core:
EC2 o contenedor

Database:
RDS PostgreSQL opcional

Serverless:
Lambda
SQS
EventBridge

Logs:
CloudWatch
```

No es obligatorio desplegar todo el sistema en AWS para demostrar la arquitectura.

---

## 16. Opción de despliegue simple

Para una prueba técnica corta:

```text
EC2
 |
 +-- Docker
      |
      +-- frontend
      +-- backend
      +-- postgres
```

Y servicios serverless separados en AWS.

No es la arquitectura ideal para producción, pero resulta suficiente para una demo reproducible.

---

## 17. Opción más cercana a producción

```text
CloudFront
   |
Frontend S3
   |
API
   |
ECS / App Runner / EC2
   |
RDS PostgreSQL
```

Serverless:

```text
EventBridge
SQS
Lambda
```

No es necesario implementar esto dentro del examen. Puede mencionarse como evolución.

---

## 18. CI/CD

Opcional si queda tiempo.

GitHub Actions:

```text
push
 |
 v
backend tests
frontend tests
build
docker build
```

Pipeline mínimo:

```text
1. checkout
2. setup Java
3. backend test
4. setup Node
5. frontend test/build
6. docker build
```

Despliegue automático es secundario frente a tener el sistema estable.

---

## 19. Seguridad de infraestructura

Principios:

- no exponer PostgreSQL;
- secretos fuera de imágenes;
- IAM mínimo;
- HTTPS en despliegue público;
- CORS limitado;
- imágenes sin herramientas innecesarias;
- dependencias actualizadas;
- usuario no-root en contenedores cuando sea posible.

---

## 20. Logging

Backend:

```text
structured logs
```

Información útil:

```text
timestamp
level
correlationId
userId
orderId
operation
duration
```

No registrar:

- contraseñas;
- tokens;
- secretos.

---

## 21. Observabilidad

Spring Actuator:

```text
/actuator/health
/actuator/info
```

Opcional:

```text
Micrometer
Prometheus
Grafana
```

No es necesario para el MVP.

---

## 22. Estrategia de demo

La demo debe poder ejecutarse de forma predecible.

### Paso 1

```bash
docker compose up
```

### Paso 2

Ingresar como dispatcher.

### Paso 3

Crear pedido.

### Paso 4

Cambiar pedido a `READY`.

### Paso 5

Procesar evento de asignación.

### Paso 6

Visualizar repartidor asignado.

### Paso 7

Completar entrega.

### Paso 8

Mostrar historial.

Idealmente no depender de servicios externos frágiles durante la demostración.

---

## 23. Datos iniciales

Puede ejecutarse un script de seed al levantar desarrollo.

Debe evitarse que los datos cambien de manera impredecible.

---

## 24. Estructura sugerida del repositorio

```text
fleetbite/
│
├── backend/
│   └── Dockerfile
│
├── frontend/
│   └── Dockerfile
│
├── infrastructure/
│   ├── docker/
│   ├── localstack/
│   └── aws/
│
├── docs/
│
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 25. Checklist de entrega

```text
[ ] docker compose levanta
[ ] PostgreSQL conserva datos
[ ] backend health OK
[ ] frontend abre
[ ] frontend consume API
[ ] Swagger accesible
[ ] seeds disponibles
[ ] variables documentadas
[ ] secretos fuera de Git
[ ] README explica ejecución
```

---

## 26. Prioridad

Orden realista:

```text
1. Docker backend
2. Docker PostgreSQL
3. Docker frontend
4. docker compose
5. health checks
6. Nginx
7. LocalStack / AWS
8. CI/CD
```

No conviene perder seis horas optimizando infraestructura mientras el botón “Entregado” todavía devuelve 500. La tecnología debe apoyar la solución, no secuestrarla.

---

## 27. Resumen

La virtualización de FleetBite se justifica porque permite encapsular:

```text
Frontend
Backend
Database
Infra local
```

en ambientes reproducibles.

El objetivo final es que una persona pueda obtener el proyecto y levantarlo con el menor número posible de pasos.
