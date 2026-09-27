package com.tigo.shop.user.service;

import com.tigo.shop.common.security.JwtService;
import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.user.domain.User;
import com.tigo.shop.user.domain.UserRepository;
import com.tigo.shop.user.dto.UserDtos.LoginRequest;
import com.tigo.shop.user.dto.UserDtos.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService("una-clave-de-pruebas-de-al-menos-32-bytes!!", 60, "test");
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, encoder, jwt);
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    private RegisterRequest request(String email) {
        return new RegisterRequest(" Ana ", "Pérez", "Zona 10", email, LocalDate.of(1995, 5, 10), "Secreta123");
    }

    @Test
    void registerHashesPasswordNormalizesEmailAndReturnsValidToken() {
        var response = service.register(request("  Ana@Correo.COM "));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@correo.com");
        assertThat(saved.getValue().getFirstName()).isEqualTo("Ana");
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("Secreta123");
        assertThat(encoder.matches("Secreta123", saved.getValue().getPasswordHash())).isTrue();

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(jwt.parse(response.accessToken())).hasValueSatisfying(u -> {
            assertThat(u.id()).isEqualTo(1L);
            assertThat(u.email()).isEqualTo("ana@correo.com");
        });
    }

    @Test
    void registerRejectsDuplicatedEmailWith409() {
        when(users.existsByEmailIgnoreCase("ana@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request("ana@correo.com")))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(users, never()).save(any());
    }

    @Test
    void loginWithWrongPasswordOrUnknownEmailReturnsSame401() {
        User user = new User();
        user.setId(1L);
        user.setEmail("ana@correo.com");
        user.setFirstName("Ana");
        user.setLastName("Pérez");
        user.setPasswordHash(encoder.encode("Secreta123"));
        when(users.findByEmailIgnoreCase("ana@correo.com")).thenReturn(Optional.of(user));
        when(users.findByEmailIgnoreCase("nadie@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ana@correo.com", "Otra1234")))
                .isInstanceOf(ApiException.class).hasMessage("Email o contraseña incorrectos");
        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@correo.com", "Otra1234")))
                .isInstanceOf(ApiException.class).hasMessage("Email o contraseña incorrectos");

        assertThat(service.login(new LoginRequest("ANA@correo.com", "Secreta123")).accessToken()).isNotBlank();
    }
}
