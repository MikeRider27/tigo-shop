package com.tigo.shop.order.service;

import com.tigo.shop.order.domain.OrderRepository;
import com.tigo.shop.order.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

/**
 * Simulador de logística para la demo: avanza el estado de las órdenes cada cierto
 * intervalo para que el usuario pueda ver el seguimiento de su pedido.
 */
@Component
@ConditionalOnProperty(name = "app.orders.auto-advance", havingValue = "true")
public class OrderStatusScheduler {

    private final OrderRepository orders;
    private final Duration interval;

    public OrderStatusScheduler(OrderRepository orders,
                                @Value("${app.orders.status-advance-interval}") Duration interval) {
        this.orders = orders;
        this.interval = interval;
    }

    @Scheduled(fixedDelayString = "${app.orders.status-advance-interval}", initialDelayString = "PT30S")
    @Transactional
    public void advanceStatuses() {
        var pending = EnumSet.of(OrderStatus.CONFIRMADA, OrderStatus.EN_PREPARACION, OrderStatus.ENVIADA);
        orders.findByStatusInAndUpdatedAtBefore(pending, Instant.now().minus(interval))
                .forEach(order -> order.advanceStatus());
    }
}
