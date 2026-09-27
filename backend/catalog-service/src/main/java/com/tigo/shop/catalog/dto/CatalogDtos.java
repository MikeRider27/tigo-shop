package com.tigo.shop.catalog.dto;

import com.tigo.shop.catalog.domain.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record ProductResponse(Long id, String sku, String name, String description, String category,
                                  BigDecimal price, int stock, String imageUrl) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(), p.getCategory(),
                    p.getPrice(), p.getStock(), p.getImageUrl());
        }
    }

    /** Página serializable con un contrato estable (Page de Spring no garantiza su formato JSON). */
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
            return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                    page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }

    public record StockItem(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record StockRequest(@NotEmpty List<@Valid StockItem> items) {
    }

    /** Precio vigente al momento de la reserva: order-service lo usa como precio oficial de la orden. */
    public record ReservedItem(Long productId, String name, BigDecimal unitPrice, int quantity, String imageUrl) {
    }
}
