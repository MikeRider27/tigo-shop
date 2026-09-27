package com.tigo.shop.order.dto;

import com.tigo.shop.order.domain.CartItem;
import com.tigo.shop.order.domain.Order;
import com.tigo.shop.order.domain.OrderItem;
import com.tigo.shop.order.domain.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    public static final int MAX_QUANTITY_PER_ITEM = 20;

    private OrderDtos() {
    }

    // ---------- Carrito ----------

    public record AddCartItemRequest(
            @NotNull(message = "El artículo es obligatorio") Long productId,
            @Min(value = 1, message = "La cantidad mínima es 1")
            @Max(value = MAX_QUANTITY_PER_ITEM, message = "La cantidad máxima por artículo es 20") int quantity) {
    }

    public record UpdateCartItemRequest(
            @Min(value = 1, message = "La cantidad mínima es 1")
            @Max(value = MAX_QUANTITY_PER_ITEM, message = "La cantidad máxima por artículo es 20") int quantity) {
    }

    public record CartItemResponse(Long productId, String name, BigDecimal unitPrice, int quantity,
                                   BigDecimal subtotal, String imageUrl) {
        public static CartItemResponse from(CartItem i) {
            return new CartItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(),
                    i.getSubtotal(), i.getImageUrl());
        }
    }

    public record CartResponse(List<CartItemResponse> items, int totalItems, BigDecimal total) {
        public static CartResponse from(List<CartItem> items) {
            var lines = items.stream().map(CartItemResponse::from).toList();
            int count = items.stream().mapToInt(CartItem::getQuantity).sum();
            BigDecimal total = lines.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new CartResponse(lines, count, total);
        }
    }

    // ---------- Órdenes ----------

    public record CheckoutRequest(
            @NotBlank(message = "La dirección de envío es obligatoria")
            @Size(max = 255, message = "La dirección no puede superar 255 caracteres") String shippingAddress) {
    }

    public record OrderItemResponse(Long productId, String name, BigDecimal unitPrice, int quantity,
                                    BigDecimal subtotal, String imageUrl) {
        public static OrderItemResponse from(OrderItem i) {
            return new OrderItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(),
                    i.getSubtotal(), i.getImageUrl());
        }
    }

    public record OrderResponse(Long id, String orderNumber, OrderStatus status, String shippingAddress,
                                BigDecimal total, Instant createdAt, Instant updatedAt,
                                List<OrderItemResponse> items) {
        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getShippingAddress(),
                    o.getTotal(), o.getCreatedAt(), o.getUpdatedAt(),
                    o.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }

    public record OrderSummaryResponse(Long id, String orderNumber, OrderStatus status, BigDecimal total,
                                       int itemCount, Instant createdAt) {
        public static OrderSummaryResponse from(Order o) {
            int count = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
            return new OrderSummaryResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getTotal(), count,
                    o.getCreatedAt());
        }
    }
}
