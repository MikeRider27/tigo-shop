package com.cart.order_service.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String VALIDATE_URL = "http://localhost:8081/auth/validate-token";

    public boolean isTokenValid(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    VALIDATE_URL,
                    HttpMethod.GET,
                    request,
                    Void.class
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (RestClientException e) {
            System.err.println("AuthClient: Token validation failed - " + e.getMessage());
            return false;
        }
    }
}
