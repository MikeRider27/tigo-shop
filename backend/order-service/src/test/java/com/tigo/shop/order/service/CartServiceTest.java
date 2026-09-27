package com.tigo.shop.order.service;

import com.tigo.shop.common.web.ApiException;
import com.tigo.shop.order.client.CatalogClient;
import com.tigo.shop.order.client.CatalogClient.ProductInfo;
import com.tigo.shop.order.domain.CartItem;
import com.tigo.shop.order.domain.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private final CartItemRepository repo = mock(CartItemRepository.class);
    private final CatalogClient catalog = mock(CatalogClient.class);
    private final CartService service = new CartService(repo, catalog);

    @Test
    void addingAnExistingProductAccumulatesQuantity() {
        CartItem existing = new CartItem(1L, 10L);
        existing.setQuantity(2);
        existing.refreshSnapshot("Balón", new BigDecimal("30.00"), "/b.svg");
        when(repo.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
        when(catalog.getProduct(10L)).thenReturn(new ProductInfo(10L, "Balón", new BigDecimal("39.90"), 5, "/b.svg"));

        service.addItem(1L, 10L, 3);

        ArgumentCaptor<CartItem> saved = ArgumentCaptor.forClass(CartItem.class);
        verify(repo).save(saved.capture());
        assertThat(saved.getValue().getQuantity()).isEqualTo(5);
        assertThat(saved.getValue().getUnitPrice()).isEqualByComparingTo("39.90");
    }

    @Test
    void cannotAddMoreThanAvailableStock() {
        when(repo.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(catalog.getProduct(10L)).thenReturn(new ProductInfo(10L, "Balón", new BigDecimal("39.90"), 2, "/b.svg"));

        assertThatThrownBy(() -> service.addItem(1L, 10L, 3))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Solo hay 2 unidades")
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(repo, never()).save(any());
    }

    @Test
    void soldOutProductCannotBeAdded() {
        when(repo.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(catalog.getProduct(10L)).thenReturn(new ProductInfo(10L, "Gorro", new BigDecimal("9.90"), 0, "/g.svg"));

        assertThatThrownBy(() -> service.addItem(1L, 10L, 1)).hasMessageContaining("agotado");
    }

    @Test
    void removingAProductNotInCartIs404() {
        when(repo.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeItem(1L, 10L))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
