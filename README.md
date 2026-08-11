# FleetBite

FleetBite es una aplicación para gestionar pedidos, repartidores, asignaciones y vehículos.
El repositorio contiene:

- backend: Java 21, Spring Boot y PostgreSQL 17;
- frontend: React, TypeScript y Vite;
- arquitectura hexagonal organizada por módulos funcionales.

Este README corresponde a la ejecución local de la rama `main`.

## Requisitos

- Docker Desktop;
- Node.js 22;
- pnpm 10, incluido mediante Corepack;
- JDK 21 únicamente si se ejecutará el backend con Maven.

## Inicio rápido local

### 1. Configurar las variables del backend

Desde la raíz del repositorio:

```powershell
Copy-Item .env.example .env
```

Edita `.env` y reemplaza, como mínimo:

```env
POSTGRES_PASSWORD=una-clave-local
SPRING_DATASOURCE_PASSWORD=una-clave-local
JWT_SECRET=un-secreto-aleatorio-de-al-menos-32-bytes
```

Las dos contraseñas de PostgreSQL deben coincidir. Para generar un secreto JWT local seguro
desde PowerShell:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

No subas `.env` al repositorio.

### 2. Levantar PostgreSQL y el backend

Desde la raíz:

```powershell
docker compose up --build -d
```

Comprueba el estado:

```powershell
docker compose ps
docker compose logs -f backend
```

### 3. Levantar el frontend

En otra terminal:

```powershell
cd frontend
corepack enable
pnpm install
pnpm dev
```

Abre la aplicación en:

```text
http://localhost:8443
```

En desarrollo, el frontend solicita rutas como `/api/v1/orders`. Vite redirige automáticamente
esas llamadas al backend local:

```text
React en http://localhost:8443
        -> /api/v1
Proxy de Vite
        -> http://localhost:8080/api/v1
```

Por eso no es necesario colocar manualmente la URL del backend para trabajar en local. Para
usar otro backend durante el desarrollo se puede definir antes de ejecutar Vite:

```powershell
$env:VITE_BACKEND_PROXY_TARGET="http://localhost:8080"
pnpm dev
```

## URLs locales

| Recurso | URL |
|---|---|
| Frontend | http://localhost:8443 |
| Backend | http://localhost:8080 |
| API REST | http://localhost:8080/api/v1 |
| Health | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI | http://localhost:8080/v3/api-docs |
| PostgreSQL | `127.0.0.1:5432` |

## Autenticación JWT

### Qué se corrigió

El backend ya no contiene un secreto JWT público de respaldo. `JWT_SECRET` es obligatorio y
debe tener al menos 32 caracteres. Si no está definido o es demasiado corto, Spring Boot no
inicia. Esto evita que una instalación olvidada pueda firmar tokens con una clave conocida.

El secreto se utiliza únicamente para firmar y verificar access tokens. No se devuelve al
frontend, no debe escribirse en Git y debe ser distinto en cada ambiente.

### Inicio de sesión

El cliente envía correo y contraseña:

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

Si las credenciales son correctas, el backend devuelve:

- un access token JWT;
- su tiempo de expiración;
- un refresh token opaco.

El JWT incluye la identidad y el rol del usuario. Spring Security verifica su firma y
expiración en cada endpoint protegido, carga la autenticación y aplica los permisos del rol.

### Uso desde el frontend

La implementación actual guarda ambos tokens en `localStorage`. En cada solicitud protegida
envía:

```http
Authorization: Bearer <access-token>
```

No se utilizan cookies de sesión en esta versión. HTTPS protege los tokens durante el
transporte en un despliegue público, pero no convierte `localStorage` en una cookie HttpOnly.

### Expiración, renovación y cierre de sesión

Los valores predeterminados son:

```env
JWT_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=604800
```

- El access token dura 3600 segundos, es decir, una hora.
- El refresh token dura 604800 segundos, es decir, siete días.
- El backend guarda solamente el hash SHA-256 del refresh token en PostgreSQL.
- Cuando el access token vence, el frontend llama a `/auth/refresh`.
- El refresh token anterior se revoca y el backend entrega un par nuevo: existe rotación.
- Al cerrar sesión, `/auth/logout` revoca el refresh token.
- Un usuario inactivo no puede renovar su sesión.

El endpoint de login también cuenta con un límite configurable por IP:

```env
LOGIN_RATE_LIMIT_MAX_ATTEMPTS=10
LOGIN_RATE_LIMIT_WINDOW_SECONDS=60
```

## Usuarios iniciales

Todos utilizan temporalmente la contraseña `Fleetbite1!` para la demostración local.

| Correo | Rol |
|---|---|
| `admin@fleetbite.local` | `ADMIN` |
| `dispatcher@fleetbite.local` | `DISPATCHER` |
| `operator@fleetbite.local` | `RESTAURANT_OPERATOR` |
| `driver@fleetbite.local` | `DRIVER` |

Estas credenciales son datos de desarrollo y no deben reutilizarse en un sistema productivo.

## Backend local sin contenedor

Se puede contenerizar solamente PostgreSQL:

```powershell
docker compose up -d postgres
cd backend
$env:JWT_SECRET="coloca-aqui-un-secreto-local-de-al-menos-32-caracteres"
.\mvnw.cmd spring-boot:run
```

En este modo el backend usa `jdbc:postgresql://localhost:5432/fleetbite`.

## Pruebas y compilación

Backend:

```powershell
cd backend
.\mvnw.cmd clean verify
```

Este comando ejecuta las pruebas unitarias, de integración, arquitectura hexagonal y la
verificación de cobertura JaCoCo.

Frontend:

```powershell
cd frontend
pnpm build
```

## Datos y contenedores

```powershell
docker compose logs -f backend
docker compose logs -f postgres
docker compose down
```

`docker compose down` conserva los datos porque PostgreSQL utiliza el volumen
`fleetbite_postgres_data`.

Para reiniciar completamente la base de datos local:

```powershell
docker compose down -v
docker compose up --build -d
```

El segundo comando elimina previamente el volumen, por lo que borra los datos locales. Flyway
reconstruye el esquema y vuelve a cargar los datos iniciales al arrancar.
