# order_service

Microservicio de órdenes de compra. Recibe las órdenes generadas por `cart_service` al confirmar un carrito y permite consultarlas.

## Stack

- Spring Boot 3.4.5 / Java 17
- Spring Security + JWT (valida contra `auth-service`)
- Spring Data JPA + PostgreSQL
- springdoc-openapi (Swagger UI)

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/ordenes` | Bearer | Crea una orden (usada internamente por `cart_service`) |
| GET | `/ordenes` | Bearer | Lista las órdenes del usuario autenticado |
| GET | `/ordenes/{numeroOrden}` | Bearer | Obtiene una orden por número (`404` si no existe) |
| GET | `/ordenes/email/{email}` | Bearer | Lista las órdenes del usuario autenticado (usa el email del token, no el del path) |

Cada orden creada recibe un `numeroOrden` autogenerado con formato `ORD-XXXXXXXX`.

Documentación interactiva: `http://<host>:8084/swagger-ui/index.html`

## Variables de entorno

| Variable | Default (dev) | Descripción |
|---|---|---|
| `DB_HOST` | `192.168.11.220` | Host de PostgreSQL |
| `DB_PORT` | `5436` | Puerto de PostgreSQL |
| `DB_NAME` | `order_db` | Base de datos |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `123` | Credenciales de la base de datos |
| `JWT_SECRET` | valor de desarrollo | Debe coincidir con el de `auth-service` |
| `CORS_ALLOWED_ORIGINS` | `localhost:3000,3001` + IP del host | Orígenes permitidos, separados por coma |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | URL base de `auth-service`, usada para validar tokens |

## Ejecutar

**Local:**
```bash
./mvnw spring-boot:run
```

**Docker:**
```bash
docker build -t order-service .
docker run -p 8084:8084 --env-file ../../.env order-service
```
(o, desde la raíz del repo, `docker compose up -d order-service`)

## Tests

```bash
./mvnw test
```

Incluye tests unitarios de `OrdenService` (generación de número de orden, vínculo orden-items, orden inexistente).
