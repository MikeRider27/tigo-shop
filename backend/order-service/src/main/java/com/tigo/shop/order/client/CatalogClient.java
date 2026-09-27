package com.tigo.shop.order.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigo.shop.common.security.InternalApiKeyFilter;
import com.tigo.shop.common.web.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

/**
 * Cliente HTTP hacia catalog-service. Traduce los errores del catálogo (404/409) a
 * errores de negocio y la indisponibilidad del servicio a 503.
 */
@Component
public class CatalogClient {

    public record ProductInfo(Long id, String name, BigDecimal price, int stock, String imageUrl) {
    }

    public record StockItem(Long productId, int quantity) {
    }

    public record ReservedItem(Long productId, String name, BigDecimal unitPrice, int quantity, String imageUrl) {
    }

    private record StockRequest(List<StockItem> items) {
    }

    private final RestClient rest;
    private final ObjectMapper mapper;

    public CatalogClient(RestClient.Builder builder,
                         ObjectMapper mapper,
                         @Value("${app.catalog.base-url}") String baseUrl,
                         @Value("${app.security.internal-api-key}") String internalApiKey) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2_000);
        requestFactory.setReadTimeout(5_000);
        this.mapper = mapper;
        this.rest = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(InternalApiKeyFilter.HEADER, internalApiKey)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw translate(response);
                })
                .build();
    }

    public ProductInfo getProduct(Long productId) {
        return call(() -> rest.get().uri("/api/products/{id}", productId).retrieve().body(ProductInfo.class));
    }

    public List<ReservedItem> reserve(List<StockItem> items) {
        return call(() -> rest.post().uri("/internal/stock/reserve")
                .body(new StockRequest(items))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ReservedItem>>() {
                }));
    }

    public void release(List<StockItem> items) {
        call(() -> rest.post().uri("/internal/stock/release")
                .body(new StockRequest(items))
                .retrieve()
                .toBodilessEntity());
    }

    private static <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El catálogo no está disponible en este momento. Intente más tarde.");
        }
    }

    private ApiException translate(ClientHttpResponse response) {
        try {
            int status = response.getStatusCode().value();
            if (status == 404 || status == 409) {
                JsonNode body = mapper.readTree(response.getBody());
                String detail = body.hasNonNull("detail") ? body.get("detail").asText() : "Error en el catálogo";
                return new ApiException(HttpStatus.valueOf(status), detail);
            }
        } catch (Exception ignored) {
            // cuerpo ilegible: se responde el error genérico
        }
        return new ApiException(HttpStatus.BAD_GATEWAY, "Error al comunicarse con el catálogo");
    }
}
