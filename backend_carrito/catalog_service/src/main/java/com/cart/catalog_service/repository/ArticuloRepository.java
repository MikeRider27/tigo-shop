package com.cart.catalog_service.repository;

import com.cart.catalog_service.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    List<Articulo> findByNombreContainingIgnoreCase(String nombre);
}
