package com.cart.cart_service.exception;

import org.springframework.http.HttpStatus;

public class ItemNotOwnedException extends ApiException {
    public ItemNotOwnedException() {
        super(HttpStatus.FORBIDDEN, "El ítem no pertenece al carrito del usuario");
    }
}
