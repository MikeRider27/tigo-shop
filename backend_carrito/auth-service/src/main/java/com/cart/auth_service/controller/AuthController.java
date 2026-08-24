package com.cart.auth_service.controller;

import com.cart.auth_service.dto.LoginRequest;
import com.cart.auth_service.dto.PasswordUpdateDto;
import com.cart.auth_service.dto.RegisterRequest;
import com.cart.auth_service.dto.UpdateProfileRequest;
import com.cart.auth_service.exception.InvalidTokenException;
import com.cart.auth_service.model.Usuario;
import com.cart.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.registrar(
                request.getNombres(),
                request.getApellidos(),
                request.getDireccionEnvio(),
                request.getEmail(),
                request.getFechaNacimiento(),
                request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("message", "Usuario registrado"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @GetMapping("/secure")
    public ResponseEntity<?> secureEndpoint(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        String email = authService.getEmailFromToken(token);
        Usuario usuario = authService.findByEmail(email);

        if (usuario == null || !authService.isValidToken(token) || !token.equals(usuario.getCurrentToken())) {
            throw new InvalidTokenException();
        }

        // Evitar retornar la contraseña y token al cliente
        usuario.setPassword(null);
        usuario.setCurrentToken(null);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateProfileRequest updateData,
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (!authService.isValidToken(token)) {
            throw new InvalidTokenException();
        }

        authService.actualizarPerfil(token, updateData);
        return ResponseEntity.ok(Collections.singletonMap("message", "Datos actualizados correctamente"));
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordUpdateDto dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (!authService.isValidToken(token)) {
            throw new InvalidTokenException();
        }

        String email = authService.getEmailFromToken(token);
        authService.updatePassword(email, dto.getOldPassword(), dto.getNewPassword());
        return ResponseEntity.ok(Collections.singletonMap("message", "Contraseña actualizada correctamente"));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        if (!authService.isValidToken(token)) {
            throw new InvalidTokenException();
        }
        return ResponseEntity.ok().build();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        return authHeader.substring(7);
    }
}
