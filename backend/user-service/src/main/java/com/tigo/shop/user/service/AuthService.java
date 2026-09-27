package com.tigo.shop.user.service;

import com.tigo.shop.common.security.AuthUser;
import com.tigo.shop.common.security.JwtService;
import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.user.domain.User;
import com.tigo.shop.user.domain.UserRepository;
import com.tigo.shop.user.dto.UserDtos.AuthResponse;
import com.tigo.shop.user.dto.UserDtos.LoginRequest;
import com.tigo.shop.user.dto.UserDtos.RegisterRequest;
import com.tigo.shop.user.dto.UserDtos.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    /** Hash ficticio para que el login tarde lo mismo exista o no el email (evita enumeración por tiempo). */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Ya existe una cuenta registrada con ese email");
        }
        User user = new User();
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(email);
        user.setShippingAddress(req.shippingAddress().trim());
        user.setBirthDate(req.birthDate());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        users.save(user);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        var user = users.findByEmailIgnoreCase(normalizeEmail(req.email()));
        String hash = user.map(User::getPasswordHash).orElse(dummyHash);
        boolean matches = passwordEncoder.matches(req.password(), hash);
        if (user.isEmpty() || !matches) {
            // Mensaje genérico: no revela si el email existe.
            throw ApiException.unauthorized("Email o contraseña incorrectos");
        }
        return toAuthResponse(user.get());
    }

    AuthResponse toAuthResponse(User user) {
        String token = jwtService.issue(
                new AuthUser(user.getId(), user.getEmail(), user.getFullName(), user.getTokenVersion()));
        return new AuthResponse(token, "Bearer", jwtService.expiresInSeconds(), UserResponse.from(user));
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
