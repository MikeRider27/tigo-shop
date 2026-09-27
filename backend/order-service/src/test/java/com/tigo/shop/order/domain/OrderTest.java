package com.tigo.shop.order.domain;

import com.tigo.shop.common.web.ApiException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void totalIsTheSumOfLineSubtotals() {
        Order order = new Order("TG-1", 1L, "Zona 10");
        order.addItem(new OrderItem(1L, "A", new BigDecimal("10.10"), 3, "/a.svg"));
        order.addItem(new OrderItem(2L, "B", new BigDecimal("0.20"), 1, "/b.svg"));

        assertThat(order.getTotal()).isEqualByComparingTo("30.50");
    }

    @Test
    void statusFlowEndsInDelivered() {
        Order order = new Order("TG-1", 1L, "Zona 10");
        order.advanceStatus();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EN_PREPARACION);
        order.advanceStatus();
        order.advanceStatus();
        order.advanceStatus();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ENTREGADA);
    }

    @Test
    void onlyConfirmedOrdersCanBeCancelled() {
        Order order = new Order("TG-1", 1L, "Zona 10");
        order.advanceStatus();

        assertThatThrownBy(order::cancel).isInstanceOf(ApiException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EN_PREPARACION);
    }
}
