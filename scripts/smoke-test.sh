#!/usr/bin/env bash
# Prueba end-to-end de todo el flujo a través del API gateway.
# Uso:  docker compose --profile test run --rm smoke-test
set -uo pipefail

BASE="${BASE_URL:-http://frontend:8080}/api"
MAILPIT="${MAILPIT_URL:-http://mailpit:8025}"
PASS=0
FAIL=0
RUN_ID=$(date +%s)

req() { # req METODO RUTA [BODY] [TOKEN] -> STATUS, BODY, HEADERS
  local args=(-s -o /tmp/body -D /tmp/headers -w '%{http_code}' -X "$1" "$BASE$2" -H 'Content-Type: application/json')
  [ -n "${4:-}" ] && args+=(-H "Authorization: Bearer $4")
  [ -n "${3:-}" ] && args+=(-d "$3")
  STATUS=$(curl "${args[@]}")
  BODY=$(cat /tmp/body)
  HEADERS=$(cat /tmp/headers)
}

ok()   { PASS=$((PASS + 1)); echo "  OK    $1"; }
fail() { FAIL=$((FAIL + 1)); echo "  FAIL  $1"; echo "        -> HTTP $STATUS $BODY"; }
expect_status() { [ "$STATUS" = "$1" ] && ok "[$STATUS] $2" || fail "[esperado $1] $2"; }
expect_true()   { if eval "$1"; then ok "$2"; else fail "$2"; fi; }
json() { echo "$BODY" | jq -r "$1"; }

EMAIL="ana.$RUN_ID@correo.com"
REGISTER="{\"firstName\":\"Ana\",\"lastName\":\"Pérez\",\"shippingAddress\":\"5a Avenida 10-20, Zona 10\",\"email\":\"$EMAIL\",\"birthDate\":\"1995-05-10\",\"password\":\"Secreta123\"}"

echo "== Registro y login =="
req POST /auth/register '{"firstName":"","lastName":"X","shippingAddress":"Y","email":"malo@x","birthDate":"2015-01-01","password":"corta"}'
expect_status 400 "registro inválido es rechazado"
expect_true '[ "$(json ".errors | keys | sort | join(\",\")")" = "birthDate,email,firstName,password" ]' \
  "errores por campo: nombres, email, menor de edad y password débil"
req POST /auth/register "$REGISTER"
expect_status 201 "registro válido"
TOKEN=$(json .accessToken)
req POST /auth/register "$REGISTER"
expect_status 409 "email duplicado"
req POST /auth/login "{\"email\":\"$EMAIL\",\"password\":\"Incorrecta1\"}"
expect_status 401 "login con contraseña incorrecta"
req POST /auth/login "{\"email\":\"${EMAIL^^}\",\"password\":\"Secreta123\"}"
expect_status 200 "login correcto (email sin distinguir mayúsculas)"

echo "== Perfil =="
req GET /users/me
expect_status 401 "perfil sin token"
req GET /users/me "" "token.falso.123"
expect_status 401 "perfil con token inválido"
req GET /users/me "" "$TOKEN"
expect_status 200 "consultar perfil"
req PUT /users/me '{"firstName":"Ana María","lastName":"Pérez","shippingAddress":"Nueva dirección 123","birthDate":"1995-05-10"}' "$TOKEN"
expect_status 200 "actualizar perfil"
expect_true '[ "$(json .firstName)" = "Ana María" ]' "el nombre se actualizó"
OLD_TOKEN=$TOKEN
req PUT /users/me/password '{"currentPassword":"Secreta123","newPassword":"Cambiada123"}' "$TOKEN"
expect_status 200 "cambiar contraseña devuelve un token nuevo"
TOKEN=$(json .accessToken)
req GET /users/me "" "$OLD_TOKEN"
expect_status 401 "el token anterior queda revocado en user-service"
req GET /cart "" "$OLD_TOKEN"
expect_status 401 "el token anterior queda revocado en order-service"
req GET /users/me "" "$TOKEN"
expect_status 200 "la sesión actual sigue activa con el token nuevo"

echo "== Catálogo =="
req GET "/products?q=bal%C3%B3n"
expect_status 200 "buscar 'balón'"
expect_true '[ "$(json .totalElements)" -ge 2 ]' "la búsqueda devuelve balones de fútbol y baloncesto"
req GET "/products?category=Tenis"
expect_true '[ "$(json "[.content[].category] | unique | join(\",\")")" = "Tenis" ]' "filtro por categoría"
req GET /products/1
STOCK_BEFORE=$(json .stock)
PRICE=$(json .price)

echo "== Carrito =="
req POST /cart/items '{"productId":1,"quantity":1}'
expect_status 401 "el carrito requiere sesión"
req POST /cart/items '{"productId":1,"quantity":2}' "$TOKEN"
expect_status 201 "agregar 2 balones"
req POST /cart/items '{"productId":1,"quantity":1}' "$TOKEN"
expect_true '[ "$(json .items[0].quantity)" = "3" ]' "agregar el mismo artículo acumula la cantidad"
req POST /cart/items '{"productId":18,"quantity":1}' "$TOKEN"
expect_status 409 "artículo agotado no se puede agregar"
req POST /cart/items '{"productId":10,"quantity":6}' "$TOKEN"
expect_status 409 "no se puede superar el stock disponible"
req POST /cart/items '{"productId":8,"quantity":1}' "$TOKEN"
req PUT /cart/items/8 '{"quantity":4}' "$TOKEN"
expect_status 200 "modificar cantidad"
req DELETE /cart/items/8 "" "$TOKEN"
expect_status 204 "eliminar artículo del carrito"
req GET /cart "" "$TOKEN"
expect_true '[ "$(json ".items | length")" = "1" ]' "el carrito quedó con 1 artículo"

echo "== Checkout y órdenes =="
req POST /orders '{"shippingAddress":""}' "$TOKEN"
expect_status 400 "la dirección de envío es obligatoria"
req POST /orders '{"shippingAddress":"Oficina: Torre Tigo, nivel 5"}' "$TOKEN"
expect_status 201 "confirmar pedido"
ORDER_ID=$(json .id)
ORDER_NUMBER=$(json .orderNumber)
expect_true '[[ "$ORDER_NUMBER" =~ ^TG-[0-9]{6}-[A-Z2-9]{6}$ ]]' "número de orden generado: $ORDER_NUMBER"
expect_true 'echo "$HEADERS" | grep -qi "^location: .*/api/orders/$ORDER_ID"' "header Location apunta a la orden"
expect_true '[ "$(jq -n "($(json .total) - $PRICE * 3) | fabs < 0.001")" = "true" ]' "total = 3 × precio ($(json .total))"
expect_true '[ "$(json .shippingAddress)" = "Oficina: Torre Tigo, nivel 5" ]' "usa la dirección editada"
req GET /cart "" "$TOKEN"
expect_true '[ "$(json .totalItems)" = "0" ]' "el carrito se vació tras confirmar"
req GET /products/1
expect_true '[ "$(json .stock)" = "$((STOCK_BEFORE - 3))" ]' "el stock se descontó ($STOCK_BEFORE -> $(json .stock))"
req POST /orders '{"shippingAddress":"X"}' "$TOKEN"
expect_status 400 "no se puede confirmar un carrito vacío"
req GET /orders "" "$TOKEN"
expect_true '[ "$(json ".[0].orderNumber")" = "$ORDER_NUMBER" ]' "la orden aparece en 'mis órdenes'"
req GET "/orders/$ORDER_ID" "" "$TOKEN"
expect_status 200 "detalle de la orden"
expect_true '[ "$(json .status)" = "CONFIRMADA" ]' "estado inicial CONFIRMADA"

req POST /auth/register "${REGISTER/$EMAIL/otro.$RUN_ID@correo.com}"
OTHER_TOKEN=$(json .accessToken)
req GET "/orders/$ORDER_ID" "" "$OTHER_TOKEN"
expect_status 404 "otro usuario no puede ver la orden (IDOR)"

req PATCH "/orders/$ORDER_ID/cancel" "" "$TOKEN"
expect_status 200 "cancelar orden"
req GET /products/1
expect_true '[ "$(json .stock)" = "$STOCK_BEFORE" ]' "el stock se devolvió al cancelar"
req PATCH "/orders/$ORDER_ID/cancel" "" "$TOKEN"
expect_status 409 "no se puede cancelar dos veces"

echo "== Seguridad del gateway =="
STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/../internal/stock/release" -H 'Content-Type: application/json' -d '{"items":[{"productId":1,"quantity":100}]}')
BODY=""
expect_true '[ "$STATUS" != "204" ]' "la API interna no es accesible desde afuera (HTTP $STATUS)"
expect_true 'curl -sI "${BASE%/api}/" | grep -qi "x-frame-options: DENY"' "headers de seguridad presentes"

echo "== Recuperación de contraseña =="
req POST /auth/forgot-password '{"email":"no-existe@correo.com"}'
expect_status 202 "email inexistente responde igual (sin enumeración)"
req POST /auth/forgot-password "{\"email\":\"$EMAIL\"}"
expect_status 202 "solicitar recuperación"
RESET_TOKEN=""
for _ in $(seq 1 20); do
  MSG_ID=$(curl -s "$MAILPIT/api/v1/search?query=to:$EMAIL" | jq -r '.messages[0].ID // empty')
  if [ -n "$MSG_ID" ]; then
    RESET_TOKEN=$(curl -s "$MAILPIT/api/v1/message/$MSG_ID" | jq -r .Text | grep -o 'token=[A-Za-z0-9_-]*' | cut -d= -f2)
    break
  fi
  sleep 0.5
done
expect_true '[ -n "$RESET_TOKEN" ]' "llegó el correo con el enlace de recuperación"
req POST /auth/reset-password "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"NuevaClave456\"}"
expect_status 204 "restablecer contraseña"
req GET /users/me "" "$TOKEN"
expect_status 401 "la recuperación revoca las sesiones abiertas (user-service)"
sleep 6 # order-service cachea la versión del token 5 s (TOKEN_VERSION_CACHE_TTL)
req GET /cart "" "$TOKEN"
expect_status 401 "la recuperación revoca las sesiones abiertas (order-service, tras el TTL de caché)"
req POST /auth/reset-password "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"OtraClave789\"}"
expect_status 400 "el enlace es de un solo uso"
req POST /auth/login "{\"email\":\"$EMAIL\",\"password\":\"NuevaClave456\"}"
expect_status 200 "login con la nueva contraseña"
TOKEN=$(json .accessToken)

echo "== Eliminar cuenta =="
req DELETE /users/me "" "$TOKEN"
expect_status 204 "eliminar cuenta"
req GET /users/me "" "$TOKEN"
expect_status 401 "el token de la cuenta eliminada ya no sirve (user-service)"
sleep 6 # la caché de order-service es por usuario: el paso anterior ya había guardado su versión
req POST /cart/items '{"productId":1,"quantity":1}' "$TOKEN"
expect_status 401 "el token de la cuenta eliminada ya no sirve (order-service, tras el TTL de caché)"
req POST /auth/login "{\"email\":\"$EMAIL\",\"password\":\"NuevaClave456\"}"
expect_status 401 "la cuenta eliminada ya no puede ingresar"

echo
echo "Resultado: $PASS OK, $FAIL fallidas"
[ "$FAIL" -eq 0 ]
