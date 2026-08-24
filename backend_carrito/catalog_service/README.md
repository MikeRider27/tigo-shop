# catalog_service

Microservicio de catálogo de artículos. Expone la consulta de artículos que usan el frontend y `cart_service` (para obtener precios al confirmar un pedido).

## Stack

- Spring Boot 3.4.5 / Java 17
- Spring Security + JWT (valida contra `auth-service`)
- Spring Data JPA + PostgreSQL
- springdoc-openapi (Swagger UI)

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/articulos` | Bearer | Lista todos los artículos |
| GET | `/articulos/{id}` | Bearer | Obtiene un artículo por id (`404` si no existe) |

Todos los endpoints requieren un JWT válido, emitido por `auth-service`. El filtro de seguridad valida el token contra `auth-service` (`services.auth.url`) y localmente contra el mismo `JWT_SECRET`.

Documentación interactiva: `http://<host>:8082/swagger-ui/index.html`

## Variables de entorno

| Variable | Default (dev) | Descripción |
|---|---|---|
| `DB_HOST` | `192.168.11.220` | Host de PostgreSQL |
| `DB_PORT` | `5436` | Puerto de PostgreSQL |
| `DB_NAME` | `catalog_db` | Base de datos |
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
docker build -t catalog-service .
docker run -p 8082:8082 --env-file ../../.env catalog-service
```
(o, desde la raíz del repo, `docker compose up -d catalog-service`)

## Tests

```bash
./mvnw test
```

Incluye tests unitarios de `ArticuloController` sobre un repositorio mockeado.
