package com.cart.order_service.repository;

import com.cart.order_service.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuarioEmail(String email);
    Orden findByNumeroOrden(String numeroOrden);
}
