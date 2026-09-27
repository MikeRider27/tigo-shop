package com.tigo.shop.user.service;

import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.user.domain.User;
import com.tigo.shop.user.domain.UserRepository;
import com.tigo.shop.user.dto.UserDtos.AuthResponse;
import com.tigo.shop.user.dto.UserDtos.ChangePasswordRequest;
import com.tigo.shop.user.dto.UserDtos.UpdateProfileRequest;
import com.tigo.shop.user.dto.UserDtos.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder, AuthService authService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(find(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = find(userId);
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setShippingAddress(req.shippingAddress().trim());
        user.setBirthDate(req.birthDate());
        return UserResponse.from(user);
    }

    /**
     * Cambia la contraseña y cierra todas las demás sesiones (los tokens anteriores quedan
     * revocados). Devuelve un token nuevo para que la sesión actual siga activa.
     */
    @Transactional
    public AuthResponse changePassword(Long userId, ChangePasswordRequest req) {
        User user = find(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("La contraseña actual no es correcta");
        }
        user.changePasswordHash(passwordEncoder.encode(req.newPassword()));
        return authService.toAuthResponse(user);
    }

    /** Al borrar la fila, todos los tokens del usuario dejan de ser válidos (el validador no lo encuentra). */
    @Transactional
    public void deleteAccount(Long userId) {
        users.delete(find(userId));
    }

    private User find(Long userId) {
        return users.findById(userId).orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
    }
}
