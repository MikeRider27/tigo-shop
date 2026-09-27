package com.tigo.shop.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lee el header "Authorization: Bearer ...". Si el token tiene firma válida, no expiró y
 * no fue revocado, registra al usuario en el SecurityContext. Si no es válido no corta
 * la cadena: la regla de autorización decide si la ruta requiere autenticación (401).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final TokenValidator tokenValidator; // puede ser null (servicio sin endpoints protegidos)

    public JwtAuthenticationFilter(JwtService jwtService, TokenValidator tokenValidator) {
        this.jwtService = jwtService;
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        Optional<AuthUser> user = header != null && header.startsWith(BEARER)
                ? jwtService.parse(header.substring(BEARER.length()))
                : Optional.empty();

        if (user.isPresent()) {
            boolean active;
            try {
                active = tokenValidator == null || tokenValidator.isActive(user.get());
            } catch (RuntimeException e) {
                // Si no se puede verificar la revocación, se falla cerrado (no se deja pasar).
                log.error("No se pudo verificar la vigencia del token", e);
                ProblemResponses.write(response, 503, "Service Unavailable",
                        "No se pudo validar la sesión. Intente nuevamente.");
                return;
            }
            if (active) {
                var auth = new UsernamePasswordAuthenticationToken(user.get(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
