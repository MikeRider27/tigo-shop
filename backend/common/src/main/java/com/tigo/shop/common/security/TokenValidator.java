package com.tigo.shop.common.security;

/**
 * Validación adicional a la firma del JWT: permite revocar tokens antes de que expiren
 * (cambio o recuperación de contraseña, cuenta eliminada). Cada servicio que tenga
 * endpoints protegidos registra su implementación como bean; si no hay ninguna, solo
 * se valida la firma y la expiración.
 */
public interface TokenValidator {

    /**
     * @return false si el token fue revocado o el usuario ya no existe.
     * @throws RuntimeException si no se puede verificar (se responde 503, nunca se deja pasar).
     */
    boolean isActive(AuthUser user);
}
