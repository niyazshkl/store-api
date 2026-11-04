CREATE TABLE IF NOT EXISTS carts(
    id BINARY(16) DEFAULT (UUID_TO_BIN(UUID())) NOT NULL,
    date_created DATE DEFAULT (CURDATE()) NOT NULL,
    CONSTRAINT `pk_carts` PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS cart_items(
    id BIGINT AUTO_INCREMENT,
    cart_id BINARY(16) NOT NULL ,
    product_id BIGINT NOT NULL ,
    quantity INTEGER DEFAULT 1 NOT NULL,
    CONSTRAINT `pk` PRIMARY KEY (id),
    CONSTRAINT `fk_cart_items_carts` FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT `fk_cart_items_products` FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT `cart_product_unique` UNIQUE KEY (cart_id, product_id)
);
