package com.tigo.shop.order.domain;

import com.tigo.shop.common.web.ApiException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /** Bloqueo optimista: evita que una cancelación y el avance de estado se sobrescriban entre sí. */
    @Version
    private long version;

    protected Order() {
    }

    public Order(String orderNumber, Long userId, String shippingAddress) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.CONFIRMADA;
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
        total = total.add(item.getSubtotal());
    }

    public void cancel() {
        if (status != OrderStatus.CONFIRMADA) {
            throw ApiException.conflict("Solo se pueden cancelar órdenes en estado CONFIRMADA (estado actual: "
                    + status + ")");
        }
        status = OrderStatus.CANCELADA;
    }

    public void advanceStatus() {
        status = status.next();
    }

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return items; }
}
