package com.tigo.shop.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Emite y valida tokens JWT firmados con HS256. El user-service emite los tokens;
 * catalog-service y order-service solo los validan con el mismo secreto.
 */
@Component
public class JwtService {

    static final String VERSION_CLAIM = "ver";

    private final SecretKey key;
    private final Duration ttl;
    private final String issuer;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-minutes:120}") long expirationMinutes,
                      @Value("${security.jwt.issuer:tigo-shop}") String issuer) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret debe tener al menos 32 bytes (256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttl = Duration.ofMinutes(expirationMinutes);
        this.issuer = issuer;
    }

    public String issue(AuthUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.id().toString())
                .issuer(issuer)
                .claim("email", user.email())
                .claim("name", user.name())
                .claim(VERSION_CLAIM, user.tokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Optional<AuthUser> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!(claims.get(VERSION_CLAIM) instanceof Number version)) {
                return Optional.empty(); // token sin versión: no se puede verificar si fue revocado
            }
            return Optional.of(new AuthUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("name", String.class),
                    version.longValue()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }
}
