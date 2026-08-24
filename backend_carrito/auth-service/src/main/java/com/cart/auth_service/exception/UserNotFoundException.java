package com.cart.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + email);
    }
}
