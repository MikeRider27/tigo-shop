// CarritoRepository.java
package com.cart.cart_service.repository;

import com.cart.cart_service.model.Carrito;
import com.cart.cart_service.model.CarritoItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
	Optional<Carrito> findByUsuarioEmailAndConfirmadoFalse(String usuarioEmail);


}
