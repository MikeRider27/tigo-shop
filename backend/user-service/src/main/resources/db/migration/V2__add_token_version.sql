-- Versión de credenciales: se incrementa al cambiar o recuperar la contraseña.
-- Los JWT llevan este número; si no coincide con el de la BD, el token está revocado.
ALTER TABLE users ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;
