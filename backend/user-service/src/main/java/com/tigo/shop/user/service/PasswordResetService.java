package com.tigo.shop.user.service;

import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.user.domain.PasswordResetToken;
import com.tigo.shop.user.domain.PasswordResetTokenRepository;
import com.tigo.shop.user.domain.User;
import com.tigo.shop.user.domain.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Flujo de recuperación de contraseña:
 * 1. forgot-password genera un token aleatorio de un solo uso (30 min) y lo envía por email.
 *    La respuesta es siempre la misma exista o no el email (no permite enumerar usuarios).
 * 2. reset-password valida el token y establece la nueva contraseña.
 */
@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailer mailer;
    private final Duration ttl;
    private final Clock clock;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder passwordEncoder, PasswordResetMailer mailer,
                                @Value("${app.password-reset-ttl-minutes:30}") long ttlMinutes,
                                Clock clock) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.clock = clock;
    }

    @Transactional
    public void requestReset(String email) {
        users.findByEmailIgnoreCase(AuthService.normalizeEmail(email)).ifPresent(user -> {
            Instant now = clock.instant();
            tokens.invalidateAllForUser(user.getId(), now);
            String rawToken = newToken();
            tokens.save(new PasswordResetToken(user.getId(), sha256(rawToken), now.plus(ttl)));
            mailer.sendResetLink(user.getEmail(), user.getFirstName(), rawToken, ttl);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        Instant now = clock.instant();
        PasswordResetToken token = tokens.findByTokenHash(sha256(rawToken))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> ApiException.badRequest("El enlace de recuperación es inválido o expiró"));
        User user = users.findById(token.getUserId())
                .orElseThrow(() -> ApiException.badRequest("El enlace de recuperación es inválido o expiró"));
        user.changePasswordHash(passwordEncoder.encode(newPassword)); // revoca las sesiones abiertas
        token.markUsed(now);
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
