package com.tigo.shop.order.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Carga las líneas en la misma consulta (evita N+1 al listar). */
    @EntityGraph(attributePaths = "items")
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Filtra también por usuario: una orden de otro usuario responde 404 (no se filtra su existencia). */
    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findByStatusInAndUpdatedAtBefore(Collection<OrderStatus> statuses, Instant before);
}
