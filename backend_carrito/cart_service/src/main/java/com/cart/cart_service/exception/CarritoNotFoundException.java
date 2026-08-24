package com.cart.cart_service.exception;

import org.springframework.http.HttpStatus;

public class CarritoNotFoundException extends ApiException {
    public CarritoNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Carrito no encontrado");
    }
}
