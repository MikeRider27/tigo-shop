# cart_service

Microservicio del carrito de compras. Gestiona el carrito activo de cada usuario y, al confirmarlo, coordina con `catalog_service` (precios) y `order_service` (creación de la orden).

## Stack

- Spring Boot 3.4.5 / Java 17
- Spring Security + JWT (valida contra `auth-service`)
- Spring Data JPA + PostgreSQL
- Bean Validation
- springdoc-openapi (Swagger UI)

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/carrito/agregar?articuloId=&cantidad=` | Bearer | Agrega un artículo al carrito activo (o incrementa la cantidad si ya está) |
| DELETE | `/carrito/remove?itemId=` | Bearer | Elimina un ítem del carrito del usuario autenticado |
| POST | `/carrito/confirm?nuevaDireccionEnvio=` | Bearer | Confirma el carrito: consulta precios en `catalog_service` y crea la orden en `order_service` |
| GET | `/carrito/pedidos` | Bearer | Lista los carritos ya confirmados del usuario |
| GET | `/carrito/items` | Bearer | Lista los ítems del carrito activo |

Documentación interactiva: `http://<host>:8093/swagger-ui/index.html`

## Variables de entorno

| Variable | Default (dev) | Descripción |
|---|---|---|
| `DB_HOST` | `192.168.11.220` | Host de PostgreSQL |
| `DB_PORT` | `5436` | Puerto de PostgreSQL |
| `DB_NAME` | `cart_db` | Base de datos |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `123` | Credenciales de la base de datos |
| `JWT_SECRET` | valor de desarrollo | Debe coincidir con el de `auth-service` |
| `CORS_ALLOWED_ORIGINS` | `localhost:3000,3001` + IP del host | Orígenes permitidos, separados por coma |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | URL base de `auth-service` |
| `CATALOG_SERVICE_URL` | `http://localhost:8082` | URL base de `catalog_service` |
| `ORDER_SERVICE_URL` | `http://localhost:8084` | URL base de `order_service` |

El servicio escucha internamente en el puerto **8083**. En `docker-compose.yml` del repo se publica en el host como **8093** (el 8083 ya estaba ocupado por otro contenedor) — ajustá el mapeo si tu entorno es distinto.

## Ejecutar

**Local:**
```bash
./mvnw spring-boot:run
```

**Docker:**
```bash
docker build -t cart-service .
docker run -p 8083:8083 --env-file ../../.env cart-service
```
(o, desde la raíz del repo, `docker compose up -d cart-service`)

## Tests

```bash
./mvnw test
```

Incluye tests unitarios de `CarritoService` (agregar/incrementar ítems, borrar ítem ajeno, carrito inexistente) sobre repositorios y clientes mockeados.
