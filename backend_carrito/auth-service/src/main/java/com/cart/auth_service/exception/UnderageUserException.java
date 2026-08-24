package com.cart.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UnderageUserException extends ApiException {
    public UnderageUserException() {
        super(HttpStatus.BAD_REQUEST, "Debe ser mayor de 18 años");
    }
}
