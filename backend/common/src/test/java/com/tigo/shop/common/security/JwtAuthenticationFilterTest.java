package com.tigo.shop.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private final JwtService jwt = new JwtService("una-clave-de-pruebas-de-al-menos-32-bytes!!", 60, "test");
    private final AuthUser user = new AuthUser(7L, "ana@correo.com", "Ana Pérez", 3);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse run(TokenValidator validator, String token) throws Exception {
        var request = new MockHttpServletRequest();
        if (token != null) request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        new JwtAuthenticationFilter(jwt, validator).doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void tokenCarriesTheVersion() {
        assertThat(jwt.parse(jwt.issue(user))).contains(user);
    }

    @Test
    void activeTokenAuthenticates() throws Exception {
        run(u -> u.tokenVersion() == 3, jwt.issue(user));

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    @Test
    void revokedTokenIsNotAuthenticated() throws Exception {
        run(u -> false, jwt.issue(user));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void failsClosedWith503WhenRevocationCannotBeChecked() throws Exception {
        var response = run(u -> { throw new IllegalStateException("user-service caído"); }, jwt.issue(user));

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void withoutValidatorOnlySignatureIsChecked() throws Exception {
        run(null, jwt.issue(user));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        SecurityContextHolder.clearContext();
        run(null, "token.falso.123");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
