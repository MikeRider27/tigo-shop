package com.cart.cart_service.exception;

import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends ApiException {
    public ItemNotFoundException(Long itemId) {
        super(HttpStatus.NOT_FOUND, "Ítem no encontrado: " + itemId);
    }
}
