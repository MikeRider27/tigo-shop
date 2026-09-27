package com.tigo.shop.user.web;

import com.tigo.shop.user.dto.UserDtos.AuthResponse;
import com.tigo.shop.user.dto.UserDtos.ForgotPasswordRequest;
import com.tigo.shop.user.dto.UserDtos.LoginRequest;
import com.tigo.shop.user.dto.UserDtos.MessageResponse;
import com.tigo.shop.user.dto.UserDtos.RegisterRequest;
import com.tigo.shop.user.dto.UserDtos.ResetPasswordRequest;
import com.tigo.shop.user.service.AuthService;
import com.tigo.shop.user.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    /** 201 Created con el token: el usuario queda autenticado tras registrarse. 409 si el email ya existe. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** 200 con el token, 401 si las credenciales son incorrectas. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** 202 Accepted siempre (no revela si el email está registrado). */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return new MessageResponse("Si el email está registrado, recibirás un enlace para restablecer tu contraseña.");
    }

    /** 204 si se cambió la contraseña, 400 si el token es inválido o expiró. */
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }
}
