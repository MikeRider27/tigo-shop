package com.tigo.shop.common.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad común a todos los microservicios:
 * API stateless, JWT en cada petición, rutas públicas definidas por propiedad
 * (app.security.public-paths) y rutas /internal/** protegidas por API key.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtService jwtService,
                                            ObjectProvider<TokenValidator> tokenValidator,
                                            @Value("${app.security.public-paths:}") String[] publicPaths,
                                            @Value("${app.security.internal-api-key}") String internalApiKey)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API stateless con Bearer token: no usa cookies de sesión
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health/**", "/internal/**", "/error").permitAll();
                    if (publicPaths.length > 0) {
                        auth.requestMatchers(publicPaths).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                ProblemResponses.write(res, 401, "Unauthorized", "Token ausente, inválido o expirado"))
                        .accessDeniedHandler((req, res, ex) ->
                                ProblemResponses.write(res, 403, "Forbidden", "Acceso denegado")))
                .addFilterBefore(new InternalApiKeyFilter(internalApiKey), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, tokenValidator.getIfAvailable()),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
