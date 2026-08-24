package com.cart.auth_service.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends ApiException {
    public EmailAlreadyRegisteredException(String email) {
        super(HttpStatus.CONFLICT, "El email ya está registrado: " + email);
    }
}
