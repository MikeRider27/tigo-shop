# Tigo Sports Shop: carrito de compras con microservicios

Solución a la **Sección 2 (caso práctico)** de la prueba técnica: una tienda responsive de artículos deportivos con
registro, login JWT, perfil, recuperación de contraseña, catálogo con búsqueda, carrito por usuario, checkout
con número de orden y seguimiento del estado de los pedidos.

| Capa | Tecnología |
|---|---|
| Frontend | React 18 + Vite + React Router (SPA responsive, sin librerías de UI) |
| Backend | Java 21 + Spring Boot 3.3 (Web, Security, Data JPA, Validation, Mail, Actuator) |
| Base de datos | MariaDB 11.4 + Flyway (migraciones versionadas) |
| Seguridad | JWT HS256 (jjwt), BCrypt, API key interna entre servicios |
| Infraestructura | Docker multi-stage + Docker Compose, nginx como API gateway |

## Cómo ejecutarlo

Requisito: **Docker** (no hace falta tener Java, Maven ni Node instalados; todo compila dentro de los contenedores).

```bash
cp .env.example .env          # y cambiar los secretos
docker compose up -d --build  # la primera vez tarda unos minutos (descarga dependencias)
```

| URL | Qué es |
|---|---|
| http://localhost:8090 | La aplicación (frontend + API en `/api`) |
| http://localhost:8025 | Mailpit: bandeja de entrada donde llegan los correos de recuperación de contraseña |

Prueba end-to-end de todo el flujo (56 verificaciones sobre el API a través del gateway):

```bash
docker compose --profile test run --rm smoke-test
```

Los **tests unitarios** (53 en backend y 10 en frontend) se ejecutan automáticamente durante `docker compose build`:
si alguno falla, la imagen no se construye.

## Arquitectura

```
                 ┌──────────────────────── Docker network ─────────────────────────┐
 Navegador ──►   │ frontend (nginx)                                                 │
  :8090          │  ├─ /            → SPA React                                     │
                 │  ├─ /api/auth, /api/users  → user-service    ──► users_db        │
                 │  ├─ /api/products          → catalog-service ──► catalog_db      │
                 │  └─ /api/cart, /api/orders → order-service   ──► orders_db       │
                 │                                   │                              │
                 │                                   └── /internal/stock/* ──► catalog-service
                 └──────────────────────────────────────────────────────────────────┘
```

- **Database-per-service**: cada microservicio tiene su esquema y un usuario de BD con permisos solo sobre él.
- **JWT con revocación**: `user-service` emite el token y los demás servicios validan la firma con el mismo secreto.
  Cada token lleva la *versión de credenciales* del usuario (`ver`); cambiar o recuperar la contraseña la incrementa
  y borrar la cuenta elimina la fila, así que los tokens anteriores dejan de valer:
  - `user-service` compara la versión contra su BD en cada petición (revocación inmediata).
  - `order-service` la consulta a `user-service` (`/internal/users/{id}/token-version`) con una caché de 5 s
    (`TOKEN_VERSION_CACHE_TTL`): un token revocado se rechaza en ≤ 5 s. Un token *más nuevo* que la caché fuerza
    la consulta, así la caché nunca rechaza un token vigente. Si `user-service` no responde, falla cerrado (503).
  - `catalog-service` no lo necesita: sus endpoints son públicos.
- **Comunicación interna**: `order-service` llama a `catalog-service` (`/internal/stock/reserve|release`) con un header
  `X-Internal-Api-Key`. nginx nunca enruta `/internal` y además borra ese header en las peticiones externas.
- **Checkout como saga con compensación**: 1) se reserva stock en el catálogo con un `UPDATE ... WHERE stock >= ?`
  atómico (todo o nada), 2) se crea la orden en una transacción local usando **los precios vigentes que devuelve el catálogo**
  (no los del carrito, que pueden estar desactualizados), 3) si falla el paso 2 se libera el stock. No se mantiene una
  transacción de BD abierta mientras se espera una llamada HTTP.
- **Seguimiento de pedidos**: CONFIRMADA → EN_PREPARACION → ENVIADA → ENTREGADA (o CANCELADA). Un scheduler simula
  la logística avanzando un estado cada 2 minutos (`ORDERS_STATUS_ADVANCE_INTERVAL`). Una orden solo se puede cancelar
  mientras está CONFIRMADA; al cancelarla se devuelve el stock. `@Version` evita que una cancelación y el scheduler se pisen.

### Estructura

```
backend/
  common/            JWT (emisión/validación), filtros de seguridad, errores RFC 7807
  user-service/      registro, login, perfil, cambio y recuperación de contraseña
  catalog-service/   catálogo fijo (seed Flyway), búsqueda paginada, reserva/liberación de stock
  order-service/     carrito por usuario, checkout, órdenes, cancelación, scheduler de estados
  Dockerfile         un solo Dockerfile multi-stage (Java 21) para los 3 servicios (--build-arg SERVICE)
frontend/            React + nginx.conf (gateway, rate limit, headers de seguridad)
db/init/             creación de esquemas y usuarios por servicio
scripts/smoke-test.sh  prueba E2E
seccion4/            ejercicios de la Sección 4 (controlador /greeting y Dockerfile de nginx)
```

## API REST

Todas las respuestas son JSON. Los errores siguen RFC 7807 (`application/problem+json`):
`{ "status": 400, "detail": "Datos inválidos", "errors": { "email": "Formato de email inválido" } }`.

| Operación | Método y ruta | Éxito | Errores |
|---|---|---|---|
| Registrar usuario | `POST /api/auth/register` | 201 + token | 400 validación, 409 email ya registrado |
| Login | `POST /api/auth/login` | 200 + token | 401 credenciales, 429 demasiados intentos |
| Solicitar recuperación | `POST /api/auth/forgot-password` | 202 (siempre) | 400 |
| Restablecer contraseña | `POST /api/auth/reset-password` | 204 | 400 token inválido/expirado/usado |
| Consultar perfil | `GET /api/users/me` | 200 | 401 |
| Modificar perfil | `PUT /api/users/me` | 200 | 400, 401 |
| Cambiar contraseña | `PUT /api/users/me/password` | 200 + token nuevo (cierra las demás sesiones) | 400, 401 |
| Borrar cuenta | `DELETE /api/users/me` | 204 | 401 |
| Buscar artículos | `GET /api/products?q=&category=&page=&size=` | 200 (paginado) | |
| Categorías | `GET /api/products/categories` | 200 | |
| Detalle de artículo | `GET /api/products/{id}` | 200 | 404 |
| Ver carrito | `GET /api/cart` | 200 | 401 |
| Agregar al carrito | `POST /api/cart/items` | 201 | 404 artículo, 409 sin stock |
| Modificar cantidad | `PUT /api/cart/items/{productId}` | 200 | 404, 409 |
| Quitar del carrito | `DELETE /api/cart/items/{productId}` | 204 | 404 |
| Vaciar carrito | `DELETE /api/cart` | 204 | |
| Confirmar pedido | `POST /api/orders` | 201 + `Location` | 400 carrito vacío, 409 sin stock, 503 catálogo caído |
| Mis órdenes | `GET /api/orders` | 200 | |
| Detalle de orden | `GET /api/orders/{id}` | 200 | 404 (también si es de otro usuario) |
| Cancelar orden | `PATCH /api/orders/{id}/cancel` | 200 | 409 si ya no está CONFIRMADA |

Las rutas de usuario usan `/me` en lugar de `/{id}`: el id sale del JWT, así un usuario no puede leer ni modificar
datos de otro (IDOR).

## Reglas de negocio implementadas

- **Registro**: nombres, apellidos, dirección de envío, email, fecha de nacimiento y contraseña son obligatorios.
  El email se valida con formato `usuario@dominio.tld` (más estricto que `@Email`, que acepta `a@b`) y se guarda en
  minúsculas. La edad mínima de 18 años se valida con una anotación propia `@Adult`. Contraseña: 8 a 72 caracteres con letras y números.
  Las mismas reglas se validan en el frontend (UX) y en el backend (seguridad).
- **Carrito**: uno por usuario y persistente en BD (sobrevive a cerrar sesión o cambiar de dispositivo). Agregar un
  artículo ya existente suma la cantidad. No se puede superar el stock disponible ni agregar artículos agotados.
- **Checkout**: muestra la dirección del perfil con opción de editarla solo para ese pedido o guardarla como
  predeterminada. Al confirmar se muestra el número de orden (`TG-AAMMDD-XXXXXX`).

## Seguridad (OWASP)

| Riesgo | Medida |
|---|---|
| Broken Access Control / IDOR | Recursos filtrados por el `userId` del JWT; una orden ajena responde 404 |
| Cryptographic Failures | BCrypt (cost 12) para contraseñas; tokens de recuperación guardados como SHA-256; secretos solo en `.env` |
| Injection | JPA con parámetros enlazados; los comodines de `LIKE` se escapan |
| Identification & Auth Failures | Rate limit en `/api/auth` (nginx), mensaje genérico en login, tiempo constante con hash ficticio, token de recuperación de un solo uso, 30 min |
| Sesiones que no se pueden revocar | Versión de token: cambiar/recuperar la contraseña o borrar la cuenta invalida todos los JWT emitidos |
| Enumeración de usuarios | `forgot-password` responde siempre 202 y envía el correo de forma asíncrona |
| Security Misconfiguration | Contenedores sin root, headers CSP/X-Frame-Options/nosniff, `server_tokens off`, sin stacktraces en respuestas |
| Integridad de precios | El total de la orden se calcula en el servidor con los precios del catálogo, nunca con lo que envía el cliente |

## Decisiones y trade-offs

- **JWT en `localStorage`**: simple para la prueba. En producción usaría cookie `HttpOnly; Secure; SameSite=Strict`
  (inmune a XSS) con access token corto + refresh token rotativo.
- **Revocación con caché de 5 s en `order-service`**: evita una llamada HTTP a `user-service` en cada petición, a
  cambio de que un token revocado pueda usarse hasta 5 s más en ese servicio. Con más servicios, la alternativa es
  publicar un evento de revocación (Kafka) o compartir un store (Redis) en vez de consultar por HTTP.
- **Consistencia eventual**: si la liberación de stock falla tras un error, hoy se registra en el log. En producción
  usaría el patrón *transactional outbox* + cola (Kafka/RabbitMQ) con reintentos.
- **Monorepo con módulo `common`**: evita duplicar la seguridad en 3 servicios. El costo es que un cambio en `common`
  obliga a redesplegar todos; en una organización grande se publicaría como librería versionada.
- **Idempotencia del checkout**: el frontend deshabilita el botón al confirmar. En producción se agregaría un header
  `Idempotency-Key` para tolerar reintentos de red sin crear pedidos duplicados.
- **Fuera de alcance**: pasarela de pagos, panel de administración del catálogo (el enunciado indica que es fijo),
  tests de integración con Testcontainers, CI/CD y observabilidad (tracing con OpenTelemetry, métricas Prometheus).

## Desarrollo local sin Docker (opcional)

```bash
# Backend (Java 21 + Maven): levantar solo la BD y Mailpit con Docker
docker compose up -d db mailpit
cd backend && mvn -pl user-service -am spring-boot:run   # requiere las variables de entorno de .env

# Frontend con hot reload (proxy de /api hacia localhost:8090)
cd frontend && npm install && npm run dev
```

## Autor

Miguel Villalba
Desarrollador Full Stack
mike.mavc27@gmail.com

## Licencia

Este proyecto está bajo la licencia MIT.
