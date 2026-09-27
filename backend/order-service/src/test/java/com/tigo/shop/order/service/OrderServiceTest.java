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
import com.tigo.shop.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository orders = mock(OrderRepository.class);
    private final CartItemRepository cartItems = mock(CartItemRepository.class);
    private final CatalogClient catalog = mock(CatalogClient.class);
    private final OrderService service = new OrderService(orders, cartItems, catalog,
            new TransactionTemplate(mock(PlatformTransactionManager.class)));

    private static CartItem cartItem(long productId, String price, int qty) {
        CartItem item = new CartItem(1L, productId);
        item.setQuantity(qty);
        item.refreshSnapshot("Producto " + productId, new BigDecimal(price), "/img.svg");
        return item;
    }

    @Test
    void emptyCartIsRejectedWith400WithoutTouchingStock() {
        when(cartItems.findByUserIdOrderByAddedAtAsc(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.checkout(1L, "Zona 10"))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(catalog);
    }

    @Test
    void checkoutUsesCatalogPricesAndClearsCart() {
        // El carrito guardó un precio viejo (30.00); la orden debe usar el vigente del catálogo (39.90).
        when(cartItems.findByUserIdOrderByAddedAtAsc(1L)).thenReturn(List.of(cartItem(10, "30.00", 2), cartItem(20, "5.00", 1)));
        when(catalog.reserve(List.of(new StockItem(10L, 2), new StockItem(20L, 1)))).thenReturn(List.of(
                new ReservedItem(10L, "Balón", new BigDecimal("39.90"), 2, "/b.svg"),
                new ReservedItem(20L, "Pelotas", new BigDecimal("8.50"), 1, "/p.svg")));
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.checkout(1L, "  Zona 10, Ciudad  ");

        assertThat(result.total()).isEqualByComparingTo("88.30");
        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMADA);
        assertThat(result.shippingAddress()).isEqualTo("Zona 10, Ciudad");
        assertThat(result.orderNumber()).matches("TG-\\d{6}-[A-Z2-9]{6}");
        assertThat(result.items()).hasSize(2);
        verify(cartItems).deleteAllByUserId(1L);
        verify(catalog, never()).release(any());
    }

    @Test
    void checkoutReleasesReservedStockWhenSavingTheOrderFails() {
        when(cartItems.findByUserIdOrderByAddedAtAsc(1L)).thenReturn(List.of(cartItem(10, "39.90", 2)));
        when(catalog.reserve(any())).thenReturn(List.of(new ReservedItem(10L, "Balón", new BigDecimal("39.90"), 2, "/b.svg")));
        when(orders.save(any(Order.class))).thenThrow(new IllegalStateException("BD caída"));

        assertThatThrownBy(() -> service.checkout(1L, "Zona 10")).isInstanceOf(IllegalStateException.class);

        verify(catalog).release(List.of(new StockItem(10L, 2)));
        verify(cartItems, never()).deleteAllByUserId(any());
    }

    @Test
    void stockConflictFromCatalogPropagatesAndNoOrderIsCreated() {
        when(cartItems.findByUserIdOrderByAddedAtAsc(1L)).thenReturn(List.of(cartItem(10, "39.90", 50)));
        doThrow(ApiException.conflict("Stock insuficiente")).when(catalog).reserve(any());

        assertThatThrownBy(() -> service.checkout(1L, "Zona 10")).hasMessage("Stock insuficiente");
        verify(orders, never()).save(any());
    }

    @Test
    void cancelReleasesStockAndOnlyWorksForConfirmedOrders() {
        Order order = new Order("TG-1", 1L, "Zona 10");
        order.addItem(new OrderItem(10L, "Balón", new BigDecimal("39.90"), 2, "/b.svg"));
        when(orders.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(order));

        assertThat(service.cancel(1L, 5L).status()).isEqualTo(OrderStatus.CANCELADA);
        verify(catalog).release(List.of(new StockItem(10L, 2)));

        assertThatThrownBy(() -> service.cancel(1L, 5L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void ordersOfOtherUsersAreNotFound() {
        when(orders.findByIdAndUserId(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder(2L, 5L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
