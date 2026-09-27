package com.tigo.shop.catalog.service;

import com.tigo.shop.catalog.domain.Product;
import com.tigo.shop.catalog.domain.ProductRepository;
import com.tigo.shop.catalog.dto.CatalogDtos.PageResponse;
import com.tigo.shop.catalog.dto.CatalogDtos.ProductResponse;
import com.tigo.shop.catalog.dto.CatalogDtos.ReservedItem;
import com.tigo.shop.catalog.dto.CatalogDtos.StockItem;
import com.tigo.shop.common.web.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CatalogService {

    static final int MAX_PAGE_SIZE = 50;

    private final ProductRepository products;

    public CatalogService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String query, String category, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("name"));
        String pattern = StringUtils.hasText(query) ? "%" + escapeLike(query.trim().toLowerCase(Locale.ROOT)) + "%" : null;
        String cat = StringUtils.hasText(category) ? category.trim() : null;
        return PageResponse.from(products.search(pattern, cat, pageable), ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return products.findCategories();
    }

    /**
     * Reserva (descuenta) el stock de todos los artículos de un pedido en una sola transacción:
     * si alguno no tiene existencias suficientes, se revierte todo (todo o nada).
     * Los ids se procesan ordenados para evitar deadlocks entre pedidos concurrentes.
     */
    @Transactional
    public List<ReservedItem> reserve(List<StockItem> items) {
        List<ReservedItem> reserved = new ArrayList<>();
        for (var entry : mergeByProduct(items).entrySet()) {
            Product product = find(entry.getKey());
            int quantity = entry.getValue();
            if (products.decrementStock(product.getId(), quantity) == 0) {
                throw ApiException.conflict("Stock insuficiente para \"" + product.getName()
                        + "\". Disponible: " + product.getStock());
            }
            reserved.add(new ReservedItem(product.getId(), product.getName(), product.getPrice(), quantity,
                    product.getImageUrl()));
        }
        return reserved;
    }

    /** Compensación: devuelve el stock cuando una orden falla o se cancela. */
    @Transactional
    public void release(List<StockItem> items) {
        mergeByProduct(items).forEach(products::incrementStock);
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> ApiException.notFound("Artículo no encontrado: " + id));
    }

    private static Map<Long, Integer> mergeByProduct(List<StockItem> items) {
        Map<Long, Integer> merged = new TreeMap<>();
        items.forEach(i -> merged.merge(i.productId(), i.quantity(), Integer::sum));
        return merged;
    }

    /** Escapa los comodines de LIKE con '!' (la barra invertida es carácter de escape en literales de MariaDB). */
    static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
