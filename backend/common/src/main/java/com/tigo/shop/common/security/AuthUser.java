package com.tigo.shop.common.security;

/**
 * Usuario autenticado extraído del JWT. Se inyecta en los controladores con
 * {@code @AuthenticationPrincipal AuthUser user}.
 *
 * @param tokenVersion versión de credenciales del usuario al emitir el token; si en la BD
 *                     ya es mayor (cambió la contraseña) el token se considera revocado.
 */
public record AuthUser(Long id, String email, String name, long tokenVersion) {
}
