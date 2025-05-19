package com.cart.cart_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carrito_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    @JsonBackReference
    private Carrito carrito;

    @Column(name = "articulo_id", nullable = false)
    private Long articuloId;

    @Column(nullable = false)
    private int cantidad;
}
