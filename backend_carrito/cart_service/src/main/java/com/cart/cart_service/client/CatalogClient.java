package com.cart.cart_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String catalogUrl;

    public CatalogClient(@Value("${services.catalog.url}") String catalogServiceUrl) {
        this.catalogUrl = catalogServiceUrl + "/articulos/";
    }

    public Double obtenerPrecioDeArticulo(Long articuloId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    catalogUrl + articuloId,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object precioObj = response.getBody().get("precio");

                if (precioObj != null) {
                    return Double.valueOf(precioObj.toString());
                } else {
                    log.warn("Precio es null para artículo {}", articuloId);
                }
            }
        } catch (Exception e) {
            log.error("Error al obtener precio del artículo {}: {}", articuloId, e.getMessage());
        }
        return 0.0;
    }
}
