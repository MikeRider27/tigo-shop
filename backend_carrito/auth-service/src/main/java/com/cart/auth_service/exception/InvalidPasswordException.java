package com.cart.auth_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ApiException {
    public InvalidPasswordException() {
        super(HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
    }
}
