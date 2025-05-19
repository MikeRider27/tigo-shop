package com.cart.cart_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;

public class JwtUtil {
    // El mismo secreto usado en auth_service
    private static final String SECRET = "clave_super_secreta_de_al_menos_32_bytes_para_seguridad"; // 256 bits (32 chars)

    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
