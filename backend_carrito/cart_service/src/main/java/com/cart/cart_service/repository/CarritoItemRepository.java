package com.cart.cart_service.repository;

import com.cart.cart_service.model.Carrito;
import com.cart.cart_service.model.CarritoItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
	List<CarritoItem> findByCarrito(Carrito carrito);
	CarritoItem findByCarritoAndArticuloId(Carrito carrito, Long articuloId);


}
