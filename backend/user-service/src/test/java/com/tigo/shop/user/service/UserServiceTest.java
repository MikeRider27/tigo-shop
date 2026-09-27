package com.tigo.shop.user.service;

import com.tigo.shop.common.security.JwtService;
import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.user.domain.User;
import com.tigo.shop.user.domain.UserRepository;
import com.tigo.shop.user.dto.UserDtos.ChangePasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService("una-clave-de-pruebas-de-al-menos-32-bytes!!", 60, "test");
    private final UserService service = new UserService(users, encoder, new AuthService(users, encoder, jwt));
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7L);
        user.setEmail("ana@correo.com");
        user.setFirstName("Ana");
        user.setLastName("Pérez");
        user.setPasswordHash(encoder.encode("Vieja1234"));
        when(users.findById(7L)).thenReturn(Optional.of(user));
    }

    @Test
    void changingPasswordRevokesOldTokensAndReturnsANewOne() {
        String oldToken = new AuthService(users, encoder, jwt).toAuthResponse(user).accessToken();

        var response = service.changePassword(7L, new ChangePasswordRequest("Vieja1234", "Nueva1234"));

        assertThat(encoder.matches("Nueva1234", user.getPasswordHash())).isTrue();
        assertThat(user.getTokenVersion()).isEqualTo(1);
        assertThat(jwt.parse(oldToken)).hasValueSatisfying(u -> assertThat(u.tokenVersion()).isZero());
        assertThat(jwt.parse(response.accessToken())).hasValueSatisfying(u -> assertThat(u.tokenVersion()).isEqualTo(1));
    }

    @Test
    void wrongCurrentPasswordKeepsSessionsAlive() {
        assertThatThrownBy(() -> service.changePassword(7L, new ChangePasswordRequest("Otra1234", "Nueva1234")))
                .isInstanceOf(ApiException.class);
        assertThat(user.getTokenVersion()).isZero();
    }
}
