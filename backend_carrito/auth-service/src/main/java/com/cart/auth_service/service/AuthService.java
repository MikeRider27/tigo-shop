package com.cart.auth_service.service;

import com.cart.auth_service.dto.UpdateProfileRequest;
import com.cart.auth_service.exception.EmailAlreadyRegisteredException;
import com.cart.auth_service.exception.InvalidCredentialsException;
import com.cart.auth_service.exception.InvalidPasswordException;
import com.cart.auth_service.exception.UnderageUserException;
import com.cart.auth_service.exception.UserNotFoundException;
import com.cart.auth_service.model.Usuario;
import com.cart.auth_service.repository.UsuarioRepository;
import com.cart.auth_service.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void registrar(String nombres, String apellidos, String direccion, String email, LocalDate nacimiento, String password) {
        if (usuarioRepo.findByEmail(email).isPresent()) {
            throw new EmailAlreadyRegisteredException(email);
        }
        if (ChronoUnit.YEARS.between(nacimiento, LocalDate.now()) < 18) {
            throw new UnderageUserException();
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
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(usuario);
        usuario.setCurrentToken(token); // Guardar token nuevo
        usuarioRepo.save(usuario);      // Persistir token en la DB

        return token;
    }

    public void actualizarPerfil(String token, UpdateProfileRequest datosActualizados) {
        String email = jwtUtil.getEmailFromToken(token);
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        usuario.setNombres(datosActualizados.getNombres());
        usuario.setApellidos(datosActualizados.getApellidos());
        usuario.setDireccionEnvio(datosActualizados.getDireccionEnvio());
        usuario.setFechaNacimiento(datosActualizados.getFechaNacimiento());

        usuarioRepo.save(usuario);
    }

    public void updatePassword(String email, String oldPassword, String newPassword) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));

        if (!passwordEncoder.matches(oldPassword, usuario.getPassword())) {
            throw new InvalidPasswordException();
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepo.save(usuario);
    }

    public boolean isValidToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.getEmailFromToken(token);
    }

    public Usuario findByEmail(String email) {
        return usuarioRepo.findByEmail(email).orElse(null);
    }
}
