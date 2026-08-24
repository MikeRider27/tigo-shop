package com.cart.auth_service.security;

import com.cart.auth_service.model.Usuario;
import com.cart.auth_service.repository.UsuarioRepository;
import com.cart.auth_service.service.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(UsuarioRepository usuarioRepository, @Lazy AuthService authService, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.replace("Bearer ", "");
            try {
                String email = jwtUtil.getEmailFromToken(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Usuario usuario = authService.findByEmail(email);

                    if (usuario != null && jwtUtil.validateToken(token) && token.equals(usuario.getCurrentToken())) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(usuario, null, new ArrayList<>());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

            } catch (JwtException e) {
                logger.warn("JWT token inválido o expirado: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
