package com.cart.order_service.model;

import com.cart.order_service.model.Orden;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orden_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonBackReference
    private Orden orden;

    @Column(name = "articulo_id", nullable = false)
    private Long articuloId;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;
}
