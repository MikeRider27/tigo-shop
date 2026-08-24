package com.cart.order_service.exception;

import org.springframework.http.HttpStatus;

public class OrdenNotFoundException extends ApiException {
    public OrdenNotFoundException(String numeroOrden) {
        super(HttpStatus.NOT_FOUND, "Orden no encontrada: " + numeroOrden);
    }
}
