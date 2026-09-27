package com.tigo.shop.catalog.service;

import com.tigo.shop.catalog.domain.Product;
import com.tigo.shop.catalog.domain.ProductRepository;
import com.tigo.shop.catalog.dto.CatalogDtos.StockItem;
import com.tigo.shop.common.web.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogServiceTest {

    private final ProductRepository repo = mock(ProductRepository.class);
    private final CatalogService service = new CatalogService(repo);

    private static Product product(long id, String name, String price, int stock) {
        return new Product(id, "SKU-" + id, name, "desc", "Fútbol", new BigDecimal(price), stock, "/img.svg");
    }

    @Test
    void searchBuildsEscapedLowercasePatternAndCapsPageSize() {
        Page<Product> empty = new PageImpl<>(List.of());
        when(repo.search(any(), any(), any())).thenReturn(empty);

        service.search("  Balón_100% ", null, -3, 500);

        verify(repo).search(eq("%balón!_100!%%"), isNull(),
                eq(org.springframework.data.domain.PageRequest.of(0, CatalogService.MAX_PAGE_SIZE,
                        org.springframework.data.domain.Sort.by("name"))));
    }

    @Test
    void blankQueryMeansNoFilter() {
        when(repo.search(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.search("   ", "", 0, 12);

        verify(repo).search(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void reserveMergesDuplicatesProcessesInIdOrderAndReturnsCurrentPrices() {
        when(repo.findById(1L)).thenReturn(Optional.of(product(1, "Balón", "39.90", 10)));
        when(repo.findById(2L)).thenReturn(Optional.of(product(2, "Tacos", "89.00", 5)));
        when(repo.decrementStock(1L, 3)).thenReturn(1);
        when(repo.decrementStock(2L, 1)).thenReturn(1);

        var reserved = service.reserve(List.of(new StockItem(2L, 1), new StockItem(1L, 2), new StockItem(1L, 1)));

        InOrder order = inOrder(repo);
        order.verify(repo).decrementStock(1L, 3);
        order.verify(repo).decrementStock(2L, 1);
        assertThat(reserved).extracting(r -> r.productId() + ":" + r.quantity() + ":" + r.unitPrice())
                .containsExactly("1:3:39.90", "2:1:89.00");
    }

    @Test
    void reserveFailsWith409WhenStockIsInsufficient() {
        when(repo.findById(1L)).thenReturn(Optional.of(product(1, "Balón", "39.90", 1)));
        when(repo.decrementStock(1L, 2)).thenReturn(0);

        assertThatThrownBy(() -> service.reserve(List.of(new StockItem(1L, 2))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Stock insuficiente")
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void reserveFailsWith404ForUnknownProduct() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reserve(List.of(new StockItem(99L, 1))))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
