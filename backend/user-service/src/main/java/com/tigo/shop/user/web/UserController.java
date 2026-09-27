package com.tigo.shop.user.web;

import com.tigo.shop.common.security.AuthUser;
import com.tigo.shop.user.dto.UserDtos.AuthResponse;
import com.tigo.shop.user.dto.UserDtos.ChangePasswordRequest;
import com.tigo.shop.user.dto.UserDtos.UpdateProfileRequest;
import com.tigo.shop.user.dto.UserDtos.UserResponse;
import com.tigo.shop.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil del usuario autenticado. Se usa "/me" en lugar de "/{id}": el id sale del JWT,
 * por lo que un usuario no puede consultar ni modificar datos de otro (IDOR).
 */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return userService.getProfile(user.id());
    }

    @PutMapping
    public UserResponse update(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(user.id(), request);
    }

    /** 200 con un token nuevo: los tokens anteriores (otras sesiones) quedan revocados. */
    @PutMapping("/password")
    public AuthResponse changePassword(@AuthenticationPrincipal AuthUser user,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(user.id(), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user) {
        userService.deleteAccount(user.id());
    }
}
