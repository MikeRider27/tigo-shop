package com.cart.catalog_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "articulos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    private Double precio;

    private Integer stock;

    @Column(name = "imagen_url")
    private String imagenUrl;
}
