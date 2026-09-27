package com.tigo.shop.user.service;

import com.tigo.shop.common.security.AuthUser;
import com.tigo.shop.common.security.TokenValidator;
import com.tigo.shop.user.domain.UserRepository;
import org.springframework.stereotype.Component;

/** user-service es dueño de los usuarios: valida la versión del token directamente en su BD. */
@Component
public class DatabaseTokenValidator implements TokenValidator {

    private final UserRepository users;

    public DatabaseTokenValidator(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean isActive(AuthUser user) {
        return users.findTokenVersionById(user.id())
                .map(version -> version == user.tokenVersion())
                .orElse(false); // cuenta eliminada
    }
}
