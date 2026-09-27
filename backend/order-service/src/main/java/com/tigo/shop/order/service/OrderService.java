package com.tigo.shop.order.service;

import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.order.client.CatalogClient;
import com.tigo.shop.order.client.CatalogClient.ReservedItem;
import com.tigo.shop.order.client.CatalogClient.StockItem;
import com.tigo.shop.order.domain.CartItem;
import com.tigo.shop.order.domain.CartItemRepository;
import com.tigo.shop.order.domain.Order;
import com.tigo.shop.order.domain.OrderItem;
import com.tigo.shop.order.domain.OrderRepository;
import com.tigo.shop.order.dto.OrderDtos.OrderResponse;
import com.tigo.shop.order.dto.OrderDtos.OrderSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Checkout como una saga simple con compensación:
 * 1. Se reserva el stock en catalog-service (llamada remota, fuera de la transacción local).
 * 2. En una transacción local se crea la orden con los precios devueltos por el catálogo y se vacía el carrito.
 * 3. Si el paso 2 falla, se libera el stock reservado (compensación).
 * Así no se mantiene abierta una transacción de BD mientras se espera una llamada HTTP.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ORDER_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final OrderRepository orders;
    private final CartItemRepository cartItems;
    private final CatalogClient catalog;
    private final TransactionTemplate tx;

    public OrderService(OrderRepository orders, CartItemRepository cartItems, CatalogClient catalog,
                        TransactionTemplate tx) {
        this.orders = orders;
        this.cartItems = cartItems;
        this.catalog = catalog;
        this.tx = tx;
    }

    public OrderResponse checkout(Long userId, String shippingAddress) {
        List<CartItem> cart = cartItems.findByUserIdOrderByAddedAtAsc(userId);
        if (cart.isEmpty()) {
            throw ApiException.badRequest("El carrito está vacío");
        }
        List<StockItem> stockItems = cart.stream()
                .map(i -> new StockItem(i.getProductId(), i.getQuantity()))
                .toList();

        List<ReservedItem> reserved = catalog.reserve(stockItems); // 409 si no hay stock

        try {
            return tx.execute(status -> {
                Order order = new Order(newOrderNumber(), userId, shippingAddress.trim());
                reserved.forEach(r -> order.addItem(
                        new OrderItem(r.productId(), r.name(), r.unitPrice(), r.quantity(), r.imageUrl())));
                orders.save(order);
                cartItems.deleteAllByUserId(userId);
                return OrderResponse.from(order);
            });
        } catch (RuntimeException e) {
            releaseQuietly(stockItems);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> listOrders(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream().map(OrderSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        return OrderResponse.from(find(userId, orderId));
    }

    /** Cancela una orden aún no preparada y devuelve el stock al catálogo. */
    public OrderResponse cancel(Long userId, Long orderId) {
        OrderResponse cancelled = tx.execute(status -> {
            Order order = find(userId, orderId);
            order.cancel();
            orders.flush();
            return OrderResponse.from(order);
        });
        releaseQuietly(cancelled.items().stream().map(i -> new StockItem(i.productId(), i.quantity())).toList());
        return cancelled;
    }

    private Order find(Long userId, Long orderId) {
        return orders.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> ApiException.notFound("Orden no encontrada"));
    }

    private void releaseQuietly(List<StockItem> items) {
        try {
            catalog.release(items);
        } catch (RuntimeException e) {
            // En producción esto iría a una cola de reintentos / outbox para garantizar consistencia eventual.
            log.error("No se pudo liberar el stock {}", items, e);
        }
    }

    static String newOrderNumber() {
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(ORDER_CODE_CHARS.charAt(RANDOM.nextInt(ORDER_CODE_CHARS.length())));
        }
        return "TG-" + LocalDate.now(ZoneOffset.UTC).format(ORDER_DATE) + "-" + code;
    }
}
