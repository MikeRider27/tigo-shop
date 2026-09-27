package com.tigo.shop.user.web;

import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.user.domain.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API interna (X-Internal-Api-Key, no expuesta por el gateway): los demás servicios
 * consultan la versión de token vigente para rechazar tokens revocados.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    public record TokenVersionResponse(long tokenVersion) {
    }

    private final UserRepository users;

    public InternalUserController(UserRepository users) {
        this.users = users;
    }

    /** 200 con la versión vigente; 404 si la cuenta fue eliminada. */
    @GetMapping("/{id}/token-version")
    public TokenVersionResponse tokenVersion(@PathVariable Long id) {
        return users.findTokenVersionById(id)
                .map(TokenVersionResponse::new)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
    }
}
