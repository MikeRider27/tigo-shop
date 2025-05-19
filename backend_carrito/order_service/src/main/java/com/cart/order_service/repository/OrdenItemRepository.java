package com.cart.order_service.repository;

import com.cart.order_service.model.OrdenItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenItemRepository extends JpaRepository<OrdenItem, Long> {
}
