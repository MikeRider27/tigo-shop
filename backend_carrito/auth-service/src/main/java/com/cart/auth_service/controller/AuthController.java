package com.cart.auth_service.controller;

import com.cart.auth_service.model.Usuario;
import com.cart.auth_service.dto.LoginRequest;
import com.cart.auth_service.dto.PasswordUpdateDto;
import com.cart.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        authService.registrar(
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getDireccionEnvio(),
                usuario.getEmail(),
                usuario.getFechaNacimiento(),
                usuario.getPassword());
        return ResponseEntity.ok("Usuario registrado");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/secure")
    public ResponseEntity<?> secureEndpoint(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = authService.getEmailFromToken(token);
        Usuario usuario = authService.findByEmail(email);

        if (usuario != null && authService.isValidToken(token) && token.equals(usuario.getCurrentToken())) {
            // Evitar retornar la contraseña y token al cliente
            usuario.setPassword(null);
            usuario.setCurrentToken(null);
            return ResponseEntity.ok(usuario);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido o usuario no encontrado");
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody Usuario updateData,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido");
        }

        String email = authService.getEmailFromToken(token);
        Usuario usuario = authService.findByEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        usuario.setNombres(updateData.getNombres());
        usuario.setApellidos(updateData.getApellidos());
        usuario.setDireccionEnvio(updateData.getDireccionEnvio());
        usuario.setFechaNacimiento(updateData.getFechaNacimiento());

        authService.save(usuario); // crea este método si no existe
        return ResponseEntity.ok("Datos actualizados correctamente");
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateDto dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido");
        }

        String email = authService.getEmailFromToken(token);
        try {
            authService.updatePassword(email, dto.getOldPassword(), dto.getNewPassword());
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (authService.isValidToken(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
}
