# FleetBite

Monorepo del core transaccional (backend Java 21 / Spring Boot + PostgreSQL 17).

## Requisitos

- Docker Desktop
- JDK 21 (solo para ejecución local con Maven / tests)

---

## Running with Docker (recomendado)

Levanta **backend + PostgreSQL** de forma reproducible.

### 1. Variables de entorno

```powershell
copy .env.example .env
```

Edita `.env` y cambia al menos:

- `POSTGRES_PASSWORD` / `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` (≥ 256 bits recomendado)

No subas `.env` al repositorio (está en `.gitignore`).

### 2. Arrancar

Desde la raíz del repo:

```powershell
docker compose up --build
```

En segundo plano:

```powershell
docker compose up --build -d
```

### 3. URLs

| Recurso   | URL                                              |
|-----------|--------------------------------------------------|
| Health    | http://localhost:8080/actuator/health            |
| Swagger   | http://localhost:8080/swagger-ui/index.html      |
| OpenAPI   | http://localhost:8080/v3/api-docs                |
| API       | http://localhost:8080/api/v1                     |

Postgres queda en `localhost:5432` (útil para DBeaver/psql en desarrollo).

Dentro de Docker, el backend usa el hostname `postgres` (no `localhost`).

### 4. Comandos útiles

```powershell
docker compose logs -f backend
docker compose logs -f postgres
docker compose ps
docker compose down
docker compose down -v
```

- `docker compose down` — detiene contenedores y **conserva** el volumen `fleetbite_postgres_data`.
- `docker compose down -v` — detiene y **elimina** el volumen (reset total de BD).

Flyway aplica V1–V10 al iniciar el backend; no hace falta ejecutar SQL manual.

### 5. Nota para un futuro EC2

El mismo Compose puede reutilizarse con secrets por env. En ese escenario PostgreSQL **no** debería exponerse públicamente; el backend quedaría detrás de reverse proxy / HTTPS. (No implementado en esta fase.)

---

## Ejecución local (sin contenedor backend)

Útil para desarrollo rápido del código Java.

### 1. Solo PostgreSQL

```powershell
docker compose up -d postgres
```

Credenciales por defecto del `.env` / compose (DB/user `fleetbite`).

### 2. Backend con Maven

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Usa `application.yaml` con `jdbc:postgresql://localhost:5432/fleetbite`.

### 3. Tests

```powershell
cd backend
.\mvnw.cmd clean test
```

Incluye `HexagonalArchitectureTest` (jMolecules + ArchUnit): verifica estereotipos hexagonales y que `domain` / `application` no dependan de `infrastructure` ni de Spring.

---

## Usuarios seed (login)

Password de todos: `Fleetbite1!`

| Email                        | Rol                  |
|------------------------------|----------------------|
| `admin@fleetbite.local`      | ADMIN                |
| `dispatcher@fleetbite.local` | DISPATCHER           |
| `operator@fleetbite.local`   | RESTAURANT_OPERATOR  |
| `driver@fleetbite.local`     | DRIVER               |

```http
POST /api/v1/auth/login
```

```json
{
  "email": "admin@fleetbite.local",
  "password": "Fleetbite1!"
}
```

---

## Si Flyway falla por datos viejos

```powershell
docker compose down -v
docker compose up --build
```
