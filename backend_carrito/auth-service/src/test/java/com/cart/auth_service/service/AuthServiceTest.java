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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepo, passwordEncoder, jwtUtil);
    }

    @Test
    void registrar_lanzaExcepcion_siEmailYaExiste() {
        when(usuarioRepo.findByEmail("existe@example.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> authService.registrar(
                "Ana", "Perez", "Calle 1", "existe@example.com", LocalDate.of(1990, 1, 1), "secret123"))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void registrar_lanzaExcepcion_siEsMenorDeEdad() {
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        LocalDate nacimientoMenor = LocalDate.now().minusYears(10);

        assertThatThrownBy(() -> authService.registrar(
                "Ana", "Perez", "Calle 1", "nueva@example.com", nacimientoMenor, "secret123"))
                .isInstanceOf(UnderageUserException.class);

        verify(usuarioRepo, never()).save(any());
    }

    @Test
    void registrar_guardaUsuarioConPasswordCifrada_cuandoDatosValidos() {
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hash-secret123");

        authService.registrar("Ana", "Perez", "Calle 1", "nueva@example.com",
                LocalDate.of(1990, 1, 1), "secret123");

        verify(usuarioRepo).save(argThatUsuarioConEmailYPassword("nueva@example.com", "hash-secret123"));
    }

    @Test
    void login_lanzaExcepcion_siUsuarioNoExiste() {
        when(usuarioRepo.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("noexiste@example.com", "cualquiera"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_lanzaExcepcion_siPasswordIncorrecta() {
        Usuario usuario = usuarioConPassword("hash-correcto");
        when(usuarioRepo.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash-correcto")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("user@example.com", "incorrecta"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_retornaToken_yLoPersisteComoTokenActual_cuandoCredencialesValidas() {
        Usuario usuario = usuarioConPassword("hash-correcto");
        when(usuarioRepo.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("correcta", "hash-correcto")).thenReturn(true);
        when(jwtUtil.generateToken(usuario)).thenReturn("token-generado");

        String token = authService.login("user@example.com", "correcta");

        assertThat(token).isEqualTo("token-generado");
        assertThat(usuario.getCurrentToken()).isEqualTo("token-generado");
        verify(usuarioRepo).save(usuario);
    }

    @Test
    void updatePassword_lanzaExcepcion_siPasswordActualIncorrecta() {
        Usuario usuario = usuarioConPassword("hash-actual");
        when(usuarioRepo.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "hash-actual")).thenReturn(false);

        assertThatThrownBy(() -> authService.updatePassword("user@example.com", "mala", "nueva123"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void updatePassword_actualizaPasswordCifrada_cuandoPasswordActualCorrecta() {
        Usuario usuario = usuarioConPassword("hash-actual");
        when(usuarioRepo.findByEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("correcta", "hash-actual")).thenReturn(true);
        when(passwordEncoder.encode("nueva123")).thenReturn("hash-nueva123");

        authService.updatePassword("user@example.com", "correcta", "nueva123");

        assertThat(usuario.getPassword()).isEqualTo("hash-nueva123");
        verify(usuarioRepo).save(usuario);
    }

    @Test
    void actualizarPerfil_lanzaExcepcion_siUsuarioNoExiste() {
        when(jwtUtil.getEmailFromToken("token")).thenReturn("fantasma@example.com");
        when(usuarioRepo.findByEmail("fantasma@example.com")).thenReturn(Optional.empty());

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNombres("Ana");
        request.setApellidos("Perez");
        request.setDireccionEnvio("Calle 1");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 1));

        assertThatThrownBy(() -> authService.actualizarPerfil("token", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    private Usuario usuarioConPassword(String passwordHash) {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@example.com");
        usuario.setPassword(passwordHash);
        return usuario;
    }

    private Usuario argThatUsuarioConEmailYPassword(String email, String password) {
        return org.mockito.ArgumentMatchers.argThat(u ->
                u != null && email.equals(u.getEmail()) && password.equals(u.getPassword()));
    }
}
