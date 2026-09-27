package com.tigo.shop.catalog.web;

import com.tigo.shop.catalog.dto.CatalogDtos.PageResponse;
import com.tigo.shop.catalog.dto.CatalogDtos.ProductResponse;
import com.tigo.shop.catalog.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** GET /api/products?q=balon&category=Fútbol&page=0&size=12 */
    @GetMapping
    public PageResponse<ProductResponse> search(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) String category,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "12") int size) {
        return catalogService.search(q, category, page, size);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return catalogService.categories();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return catalogService.get(id);
    }
}
