package com.cart.cart_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "carritos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_email", nullable = false)
    private String usuarioEmail;

    @Column(name = "direccion_envio", nullable = false)
    private String direccionEnvio;

    @Column(nullable = false)
    private boolean confirmado = false;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CarritoItem> items;

}
