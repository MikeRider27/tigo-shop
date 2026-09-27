package com.tigo.shop.user.service;

import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.user.domain.PasswordResetToken;
import com.tigo.shop.user.domain.PasswordResetTokenRepository;
import com.tigo.shop.user.domain.User;
import com.tigo.shop.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private final Instant now = Instant.parse("2026-09-27T12:00:00Z");
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
    private final PasswordResetMailer mailer = mock(PasswordResetMailer.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(users, tokens, encoder, mailer, 30, Clock.fixed(now, ZoneOffset.UTC));
        user = new User();
        user.setId(7L);
        user.setEmail("ana@correo.com");
        user.setFirstName("Ana");
        user.setPasswordHash(encoder.encode("Vieja1234"));
    }

    @Test
    void unknownEmailDoesNothingAndDoesNotFail() {
        when(users.findByEmailIgnoreCase("nadie@correo.com")).thenReturn(Optional.empty());

        service.requestReset("nadie@correo.com");

        verifyNoInteractions(mailer, tokens);
    }

    @Test
    void storesOnlyTheHashAndMailsTheRawToken() {
        when(users.findByEmailIgnoreCase("ana@correo.com")).thenReturn(Optional.of(user));

        service.requestReset("ana@correo.com");

        ArgumentCaptor<String> rawToken = ArgumentCaptor.forClass(String.class);
        verify(mailer).sendResetLink(eq("ana@correo.com"), eq("Ana"), rawToken.capture(), eq(Duration.ofMinutes(30)));
        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokens).save(saved.capture());
        verify(tokens).invalidateAllForUser(7L, now);

        assertThat(saved.getValue().getTokenHash())
                .isNotEqualTo(rawToken.getValue())
                .isEqualTo(PasswordResetService.sha256(rawToken.getValue()));
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(30)));
    }

    @Test
    void resetChangesPasswordAndConsumesToken() {
        var token = new PasswordResetToken(7L, PasswordResetService.sha256("raw"), now.plusSeconds(60));
        when(tokens.findByTokenHash(PasswordResetService.sha256("raw"))).thenReturn(Optional.of(token));
        when(users.findById(7L)).thenReturn(Optional.of(user));

        service.resetPassword("raw", "Nueva1234");

        assertThat(encoder.matches("Nueva1234", user.getPasswordHash())).isTrue();
        assertThat(token.getUsedAt()).isEqualTo(now);
        assertThat(user.getTokenVersion()).as("las sesiones abiertas quedan revocadas").isEqualTo(1);
    }

    @Test
    void expiredTokenIsRejected() {
        var token = new PasswordResetToken(7L, PasswordResetService.sha256("raw"), now.minusSeconds(1));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("raw", "Nueva1234"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("inválido o expiró");
    }

    @Test
    void usedTokenIsRejected() {
        var token = new PasswordResetToken(7L, PasswordResetService.sha256("raw"), now.plusSeconds(60));
        token.markUsed(now.minusSeconds(5));
        when(tokens.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("raw", "Nueva1234")).isInstanceOf(ApiException.class);
    }
}
