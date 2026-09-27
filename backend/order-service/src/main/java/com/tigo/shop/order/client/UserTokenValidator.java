package com.tigo.shop.order.client;

import com.tigo.shop.common.security.AuthUser;
import com.tigo.shop.common.security.InternalApiKeyFilter;
import com.tigo.shop.common.security.TokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verifica contra user-service (dueño de los usuarios) que el token no haya sido revocado.
 * La versión vigente se cachea unos segundos para no hacer una llamada HTTP por petición:
 * la revocación se hace efectiva aquí en, como máximo, ese TTL. Si user-service no responde,
 * el filtro falla cerrado (503).
 */
@Component
public class UserTokenValidator implements TokenValidator {

    private record CachedVersion(Long version, Instant expiresAt) {
    }

    private record TokenVersionResponse(long tokenVersion) {
    }

    private static final Long DELETED = null;

    private final RestClient rest;
    private final Duration ttl;
    private final Clock clock;
    private final Map<Long, CachedVersion> cache = new ConcurrentHashMap<>();

    public UserTokenValidator(RestClient.Builder builder,
                              @Value("${app.users.base-url}") String baseUrl,
                              @Value("${app.security.internal-api-key}") String internalApiKey,
                              @Value("${app.security.token-version-cache-ttl:PT5S}") Duration ttl,
                              Clock clock) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1_000);
        requestFactory.setReadTimeout(2_000);
        this.rest = builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(InternalApiKeyFilter.HEADER, internalApiKey)
                .build();
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public boolean isActive(AuthUser user) {
        Instant now = clock.instant();
        CachedVersion cached = cache.get(user.id());
        // Las versiones solo aumentan: un token más nuevo que la caché significa que la caché
        // está desactualizada (p. ej. recién cambió la contraseña) y se consulta de inmediato.
        // Así la caché solo puede demorar un rechazo, nunca rechazar un token vigente.
        boolean stale = cached == null
                || now.isAfter(cached.expiresAt())
                || (cached.version() != null && user.tokenVersion() > cached.version());
        if (stale) {
            cached = new CachedVersion(fetchVersion(user.id()), now.plus(ttl));
            cache.put(user.id(), cached);
        }
        return cached.version() != null && cached.version() == user.tokenVersion();
    }

    private Long fetchVersion(Long userId) {
        try {
            var response = rest.get().uri("/internal/users/{id}/token-version", userId)
                    .retrieve()
                    .body(TokenVersionResponse.class);
            return response.tokenVersion();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return DELETED;
            }
            throw e;
        }
    }
}
