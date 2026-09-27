package com.tigo.shop.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 120)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, updatable = false)
    private Instant addedAt = Instant.now();

    protected CartItem() {
    }

    public CartItem(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    /** Actualiza la foto del artículo (nombre/precio/imagen) con los datos vigentes del catálogo. */
    public void refreshSnapshot(String productName, BigDecimal unitPrice, String imageUrl) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.imageUrl = imageUrl;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Instant getAddedAt() { return addedAt; }
}
