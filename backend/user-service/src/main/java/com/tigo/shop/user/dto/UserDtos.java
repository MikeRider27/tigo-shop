package com.tigo.shop.user.dto;

import com.tigo.shop.user.domain.User;
import com.tigo.shop.user.validation.Adult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import static com.tigo.shop.user.validation.ValidationRules.EMAIL_MESSAGE;
import static com.tigo.shop.user.validation.ValidationRules.EMAIL_REGEX;
import static com.tigo.shop.user.validation.ValidationRules.PASSWORD_MESSAGE;
import static com.tigo.shop.user.validation.ValidationRules.PASSWORD_REGEX;

/** Contratos de entrada/salida del user-service. La entidad JPA nunca se expone directamente. */
public final class UserDtos {

    private UserDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Los nombres son obligatorios") @Size(max = 80) String firstName,
            @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String lastName,
            @NotBlank(message = "La dirección de envío es obligatoria") @Size(max = 255) String shippingAddress,
            @NotBlank(message = "El email es obligatorio") @Size(max = 160)
            @Pattern(regexp = EMAIL_REGEX, message = EMAIL_MESSAGE) String email,
            @NotNull(message = "La fecha de nacimiento es obligatoria") @Adult LocalDate birthDate,
            @NotBlank(message = "La contraseña es obligatoria")
            @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE) String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "El email es obligatorio") String email,
            @NotBlank(message = "La contraseña es obligatoria") String password) {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "Los nombres son obligatorios") @Size(max = 80) String firstName,
            @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String lastName,
            @NotBlank(message = "La dirección de envío es obligatoria") @Size(max = 255) String shippingAddress,
            @NotNull(message = "La fecha de nacimiento es obligatoria") @Adult LocalDate birthDate) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "La contraseña actual es obligatoria") String currentPassword,
            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE) String newPassword) {
    }

    public record ForgotPasswordRequest(
            @NotBlank(message = "El email es obligatorio")
            @Pattern(regexp = EMAIL_REGEX, message = EMAIL_MESSAGE) String email) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "El token es obligatorio") String token,
            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE) String newPassword) {
    }

    public record UserResponse(Long id, String firstName, String lastName, String email,
                               String shippingAddress, LocalDate birthDate) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                    u.getShippingAddress(), u.getBirthDate());
        }
    }

    public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {
    }

    public record MessageResponse(String message) {
    }
}
