package com.tigo.shop.order.client;

import com.sun.net.httpserver.HttpServer;
import com.tigo.shop.common.security.AuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba contra un servidor HTTP real (JDK) que simula el endpoint interno de user-service. */
class UserTokenValidatorTest {

    private HttpServer server;
    private final AtomicInteger calls = new AtomicInteger();
    private volatile int status = 200;
    private volatile long currentVersion = 0;
    private volatile String receivedKey;
    private Instant now = Instant.parse("2026-09-27T12:00:00Z");
    private UserTokenValidator validator;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal/users/7/token-version", exchange -> {
            calls.incrementAndGet();
            receivedKey = exchange.getRequestHeaders().getFirst("X-Internal-Api-Key");
            byte[] body = ("{\"tokenVersion\":" + currentVersion + "}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, status == 200 ? body.length : -1);
            if (status == 200) exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        Clock clock = new Clock() {
            @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId zone) { return this; }
            @Override public Instant instant() { return now; }
        };
        validator = new UserTokenValidator(RestClient.builder(),
                "http://127.0.0.1:" + server.getAddress().getPort(), "clave-interna", Duration.ofSeconds(5), clock);
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private static AuthUser tokenWithVersion(long version) {
        return new AuthUser(7L, "ana@correo.com", "Ana", version);
    }

    @Test
    void currentVersionIsActiveAndSendsInternalKey() {
        assertThat(validator.isActive(tokenWithVersion(0))).isTrue();
        assertThat(receivedKey).isEqualTo("clave-interna");
    }

    @Test
    void revokedTokenIsRejectedOnceTheCacheExpires() {
        assertThat(validator.isActive(tokenWithVersion(0))).isTrue();
        currentVersion = 1; // el usuario cambió su contraseña

        assertThat(validator.isActive(tokenWithVersion(0))).as("dentro del TTL se usa la caché").isTrue();
        assertThat(calls).hasValue(1);

        now = now.plusSeconds(6);
        assertThat(validator.isActive(tokenWithVersion(0))).isFalse();
        assertThat(validator.isActive(tokenWithVersion(1))).isTrue();
        assertThat(calls).hasValue(2);
    }

    @Test
    void newTokenAfterPasswordChangeIsAcceptedImmediatelyDespiteTheCache() {
        assertThat(validator.isActive(tokenWithVersion(0))).isTrue(); // caché: versión 0
        currentVersion = 1;                                            // cambió la contraseña

        assertThat(validator.isActive(tokenWithVersion(1))).as("no debe esperar el TTL").isTrue();
        assertThat(validator.isActive(tokenWithVersion(0))).as("y el viejo queda rechazado ya").isFalse();
        assertThat(calls).hasValue(2);
    }

    @Test
    void deletedUserIsRejected() {
        status = 404;
        assertThat(validator.isActive(tokenWithVersion(0))).isFalse();
    }

    @Test
    void userServiceErrorPropagatesSoTheFilterFailsClosed() {
        status = 500;
        assertThatThrownBy(() -> validator.isActive(tokenWithVersion(0))).isInstanceOf(RestClientException.class);
    }
}
