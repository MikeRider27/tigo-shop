package com.cart.cart_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthClient {

    private static final Logger log = LoggerFactory.getLogger(AuthClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String validateUrl;

    public AuthClient(@Value("${services.auth.url}") String authServiceUrl) {
        this.validateUrl = authServiceUrl + "/auth/validate-token";
    }

    public boolean isTokenValid(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    validateUrl,
                    HttpMethod.GET,
                    request,
                    Void.class
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (RestClientException e) {
            log.warn("AuthClient: Token validation failed - {}", e.getMessage());
            return false;
        }
    }
}
