-- user_id / product_id son referencias lógicas a otros microservicios (database-per-service),
-- por eso no hay FOREIGN KEY hacia users ni products.
CREATE TABLE cart_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(120)   NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    image_url    VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    added_at     DATETIME(6)    NOT NULL,
    CONSTRAINT uk_cart_user_product UNIQUE (user_id, product_id),
    CONSTRAINT ck_cart_quantity CHECK (quantity > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number     VARCHAR(30)    NOT NULL,
    user_id          BIGINT         NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    shipping_address VARCHAR(255)   NOT NULL,
    total            DECIMAL(12, 2) NOT NULL,
    created_at       DATETIME(6)    NOT NULL,
    updated_at       DATETIME(6)    NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_orders_number UNIQUE (order_number)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_orders_user_created ON orders (user_id, created_at);
CREATE INDEX ix_orders_status_updated ON orders (status, updated_at);

CREATE TABLE order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(120)   NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    quantity     INT            NOT NULL,
    subtotal     DECIMAL(12, 2) NOT NULL,
    image_url    VARCHAR(255)   NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
