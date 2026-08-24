# auth-service

Microservicio de autenticación y gestión de usuarios. Emite y valida los JWT que usan el resto de los microservicios (`catalog_service`, `cart_service`, `order_service`).

## Stack

- Spring Boot 3.4.5 / Java 17
- Spring Security + JWT (jjwt)
- Spring Data JPA + PostgreSQL
- Bean Validation
- springdoc-openapi (Swagger UI)

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/auth/register` | No | Registra un usuario nuevo (requiere ser mayor de 18 años) |
| POST | `/auth/login` | No | Login; devuelve `{ "token": "..." }` |
| GET | `/auth/secure` | Bearer | Devuelve el perfil del usuario autenticado (sin password/token) |
| PUT | `/auth/update` | Bearer | Actualiza datos de perfil (nombre, apellido, dirección, fecha de nacimiento) |
| PUT | `/auth/update-password` | Bearer | Cambia la contraseña (requiere la contraseña actual) |
| GET | `/auth/validate-token` | Bearer | `200` si el token es válido, `401` si no |

Todas las respuestas de error siguen el formato:
```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "..." }
```
Los errores de validación (`400`) incluyen además un objeto `errors` con el detalle por campo.

Documentación interactiva: `http://<host>:8081/swagger-ui/index.html`

## Variables de entorno

| Variable | Default (dev) | Descripción |
|---|---|---|
| `DB_HOST` | `192.168.11.220` | Host de PostgreSQL |
| `DB_PORT` | `5436` | Puerto de PostgreSQL |
| `DB_NAME` | `auth_db` | Base de datos |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `123` | Credenciales de la base de datos |
| `JWT_SECRET` | valor de desarrollo | Secreto para firmar los JWT — debe coincidir con el de los demás servicios |
| `JWT_EXPIRATION_MS` | `86400000` (1 día) | Tiempo de vida del token |
| `CORS_ALLOWED_ORIGINS` | `localhost:3000,3001` + IP del host | Orígenes permitidos, separados por coma |

## Ejecutar

**Local:**
```bash
./mvnw spring-boot:run
```

**Docker:**
```bash
docker build -t auth-service .
docker run -p 8081:8081 --env-file ../../.env auth-service
```
(o, desde la raíz del repo, `docker compose up -d auth-service`)

## Tests

```bash
./mvnw test
```

Incluye tests unitarios de `AuthService` (registro, login, cambio de contraseña) sobre mocks, sin depender de la base de datos.
