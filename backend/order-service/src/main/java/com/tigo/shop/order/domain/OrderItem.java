package com.tigo.shop.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Línea de una orden. Guarda nombre y precio al momento de la compra (no cambian si el catálogo cambia). */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 120)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private String imageUrl;

    protected OrderItem() {
    }

    public OrderItem(Long productId, String productName, BigDecimal unitPrice, int quantity, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getImageUrl() { return imageUrl; }
}
