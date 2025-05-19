package com.cart.cart_service.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String CATALOG_URL = "http://localhost:8082/articulos/";

    public Double obtenerPrecioDeArticulo(Long articuloId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    CATALOG_URL + articuloId,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            System.out.println("Respuesta artículo " + articuloId + ": " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object precioObj = response.getBody().get("precio");

                if (precioObj != null) {
                    return Double.valueOf(precioObj.toString());
                } else {
                    System.err.println("Precio es null para artículo " + articuloId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener precio del artículo " + articuloId + ": " + e.getMessage());
        }
        return 0.0;
    }
}
