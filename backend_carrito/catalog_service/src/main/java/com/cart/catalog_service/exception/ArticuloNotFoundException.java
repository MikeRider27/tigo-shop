package com.cart.catalog_service.exception;

import org.springframework.http.HttpStatus;

public class ArticuloNotFoundException extends ApiException {
    public ArticuloNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Artículo no encontrado: " + id);
    }
}
