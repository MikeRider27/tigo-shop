package com.cart.cart_service.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OrderClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ORDER_URL = "http://localhost:8084/ordenes"; // puerto del order_service

    public ResponseEntity<String> enviarOrden(Map<String, Object> ordenJson, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ordenJson, headers);

        return restTemplate.exchange(
                ORDER_URL,
                HttpMethod.POST,
                entity,
                String.class
        );
    }
} 