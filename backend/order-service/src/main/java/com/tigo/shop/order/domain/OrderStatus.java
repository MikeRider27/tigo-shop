package com.tigo.shop.order.domain;

public enum OrderStatus {
    CONFIRMADA,
    EN_PREPARACION,
    ENVIADA,
    ENTREGADA,
    CANCELADA;

    /** Siguiente estado del flujo logístico, o el mismo si es un estado final. */
    public OrderStatus next() {
        return switch (this) {
            case CONFIRMADA -> EN_PREPARACION;
            case EN_PREPARACION -> ENVIADA;
            case ENVIADA, ENTREGADA -> ENTREGADA;
            case CANCELADA -> CANCELADA;
        };
    }

    public boolean isFinal() {
        return this == ENTREGADA || this == CANCELADA;
    }
}
