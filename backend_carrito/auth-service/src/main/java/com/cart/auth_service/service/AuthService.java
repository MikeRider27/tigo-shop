package com.cart.auth_service.service;

import com.cart.auth_service.model.Usuario;
import com.cart.auth_service.config.SecurityConfig;
import com.cart.auth_service.repository.UsuarioRepository;
import com.cart.auth_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    public void registrar(String nombres, String apellidos, String direccion, String email, LocalDate nacimiento, String password) {
        if (usuarioRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }
        if (ChronoUnit.YEARS.between(nacimiento, LocalDate.now()) < 18) {
            throw new RuntimeException("Debe ser mayor de 18 años");
        }
        Usuario usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setDireccionEnvio(direccion);
        usuario.setEmail(email);
        usuario.setFechaNacimiento(nacimiento);
        usuario.setPassword(passwordEncoder.encode(password));
        usuarioRepo.save(usuario);
    }

    public String login(String email, String password) {
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("No encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = JwtUtil.generateToken(usuario);
        usuario.setCurrentToken(token); // Guardar token nuevo
        usuarioRepo.save(usuario);      // Persistir token en la DB

        return token;
    }
    
    public void actualizarPerfil(String token, Usuario datosActualizados) {
        String email = JwtUtil.getEmailFromToken(token);
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombres(datosActualizados.getNombres());
        usuario.setApellidos(datosActualizados.getApellidos());
        usuario.setDireccionEnvio(datosActualizados.getDireccionEnvio());
        usuario.setFechaNacimiento(datosActualizados.getFechaNacimiento());

        usuarioRepo.save(usuario);
    }

    public void updatePassword(String email, String oldPassword, String newPassword) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
        if (!passwordEncoder.matches(oldPassword, usuario.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }
    
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepo.save(usuario);
    }
    



    
    public boolean isValidToken(String token) {
        return JwtUtil.validateToken(token);
    }

    public String getEmailFromToken(String token) {
        return JwtUtil.getEmailFromToken(token);
    }

    public Usuario findByEmail(String email) {
    	return usuarioRepo.findByEmail(email).orElse(null);
    }
    
    public void save(Usuario usuario) {
        usuarioRepo.save(usuario);
    }


}
