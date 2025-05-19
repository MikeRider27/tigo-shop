package com.cart.auth_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombres;
    private String apellidos;
    private String direccionEnvio;

    @Column(unique = true)
    private String email;

    private LocalDate fechaNacimiento;

    private String password;

    private boolean activo = true;

    @Column(name = "current_token", columnDefinition = "TEXT")
    private String currentToken;

}

