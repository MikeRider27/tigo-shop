package com.cart.order_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", nullable = false, unique = true)
    private String numeroOrden;

    @Column(name = "usuario_email", nullable = false)
    private String usuarioEmail;

    @Column(name = "direccion_envio", nullable = false, columnDefinition = "TEXT")
    private String direccionEnvio;

    @Column(nullable = false)
    private String estado = "pendiente";

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenItem> items;
}

