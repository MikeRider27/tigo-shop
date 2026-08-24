# 🛒 Carrito de Compras

Aplicación de carrito de compras basada en microservicios: un frontend en **ReactJS** y un backend de **4 microservicios independientes en Spring Boot**, cada uno con su propia base de datos. Todo el stack se orquesta con **Docker Compose**.

## 🧱 Arquitectura

```
                     ┌──────────────┐
                     │   Frontend    │  React (puerto 3001)
                     └───────┬──────┘
                             │
        ┌────────────┬───────┴────────┬─────────────┐
        ▼            ▼                ▼             ▼
 ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
 │ auth-service│ │catalog-service│ │cart-service │ │order-service│
 │   :8081     │ │    :8082     │ │   :8093*    │ │   :8084     │
 └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
        │               │               │               │
        ▼               ▼               ▼               ▼
   auth_db          catalog_db       cart_db         order_db
        └───────────────┴───── PostgreSQL ───────────┘
```

`cart-service` además llama internamente a `auth-service` (validar token), `catalog-service` (precio de artículos) y `order-service` (crear la orden) por la red interna de Docker.

*\* `cart-service` escucha en el puerto 8083 dentro del contenedor; se publica en 8093 en el host por defecto (ver nota de puertos más abajo).*

| Servicio | Puerto (host) | Base de datos | README |
|---|---|---|---|
| Frontend (React) | 3001 | — | — |
| auth-service | 8081 | `auth_db` | [backend_carrito/auth-service](backend_carrito/auth-service/README.md) |
| catalog_service | 8082 | `catalog_db` | [backend_carrito/catalog_service](backend_carrito/catalog_service/README.md) |
| cart_service | 8093 → 8083 | `cart_db` | [backend_carrito/cart_service](backend_carrito/cart_service/README.md) |
| order_service | 8084 | `order_db` | [backend_carrito/order_service](backend_carrito/order_service/README.md) |

Cada microservicio expone su propia documentación **Swagger UI** en `http://<host>:<puerto>/swagger-ui/index.html`.

## 🚀 Requisitos

- [Docker](https://www.docker.com/) y [Docker Compose](https://docs.docker.com/compose/)
- Una instancia de **PostgreSQL** accesible (host/puerto configurables por variables de entorno; no se levanta por Docker Compose)
- Java 17 y Maven (solo si querés correr algún servicio fuera de Docker)

## ⚙️ Configuración

### Frontend (`.env` en la raíz)

```env
PORT=3000
REACT_APP_AUTH_URL=http://<host>:8081
REACT_APP_CATALOG_URL=http://<host>:8082
REACT_APP_CART_URL=http://<host>:8093
REACT_APP_ORDER_URL=http://<host>:8084
```

### Backend (variables de entorno, con defaults de desarrollo si no se definen)

| Variable | Descripción | Default |
|---|---|---|
| `DB_HOST` / `DB_PORT` | Host/puerto de PostgreSQL | `192.168.11.220` / `5436` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de la base de datos | `postgres` / `123` |
| `JWT_SECRET` | Secreto para firmar/validar JWT (**debe ser el mismo en los 4 servicios**) | valor de desarrollo, cambiar en producción |
| `JWT_EXPIRATION_MS` | Expiración del token (solo `auth-service`) | `86400000` (1 día) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma | `localhost:3000/3001` y la IP del host |

Cada servicio usa su propia base de datos (`auth_db`, `catalog_db`, `cart_db`, `order_db`), creadas automáticamente si el usuario de PostgreSQL tiene permisos, o manualmente con:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE catalog_db;
CREATE DATABASE cart_db;
CREATE DATABASE order_db;
```

## 🐳 Ejecutar todo con Docker

Desde la raíz del proyecto:

```bash
docker compose up -d --build
```

Esto construye y levanta el frontend y los 4 microservicios en una red compartida. Si algún puerto (3000, 8081-8084) ya está ocupado por otro contenedor tuyo, ajustá el mapeo en `docker-compose.yml` (por ejemplo, `cart-service` y el frontend ya están remapeados a 8093 y 3001 respectivamente por este motivo).

Para levantar solo el backend:

```bash
docker compose up -d auth-service catalog-service cart-service order-service
```

## 🧪 Desarrollo sin Docker

**Frontend:**
```bash
npm install
npm start
```

**Cada microservicio** (ver también su propio README):
```bash
cd backend_carrito/<servicio>
./mvnw spring-boot:run
```

## 📦 Stack

- **Frontend:** React 18, Axios, React Router DOM, React Hook Form + Yup, Bootstrap
- **Backend:** Spring Boot 3.4.5, Java 17, Spring Security + JWT, Spring Data JPA, PostgreSQL, springdoc-openapi (Swagger)
- **Infraestructura:** Docker, Docker Compose

## 🔐 Notas de seguridad

- La autenticación es vía JWT: `auth-service` emite el token; `catalog-service`, `cart-service` y `order-service` lo validan (localmente y/o contra `auth-service`).
- Todos los secretos y credenciales se configuran por variable de entorno — no hay valores sensibles hardcodeados en el código para uso en producción; los defaults en `docker-compose.yml` son solo para desarrollo local.

---

© 2025 - Proyecto desarrollado por Miguel Angel Villalba Cabañas
