package com.tigo.shop.catalog.web;

import com.tigo.shop.catalog.dto.CatalogDtos.ReservedItem;
import com.tigo.shop.catalog.dto.CatalogDtos.StockRequest;
import com.tigo.shop.catalog.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API interna (servicio a servicio) usada por order-service. Requiere el header
 * X-Internal-Api-Key y no se publica a través del gateway.
 */
@RestController
@RequestMapping("/internal/stock")
public class InternalStockController {

    private final CatalogService catalogService;

    public InternalStockController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** 200 con precios vigentes; 409 si algún artículo no tiene stock; 404 si no existe. */
    @PostMapping("/reserve")
    public List<ReservedItem> reserve(@Valid @RequestBody StockRequest request) {
        return catalogService.reserve(request.items());
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@Valid @RequestBody StockRequest request) {
        catalogService.release(request.items());
    }
}
