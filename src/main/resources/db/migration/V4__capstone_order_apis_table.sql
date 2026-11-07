create table orders
(
    id          BIGINT                           not null auto_increment PRIMARY KEY,
    customer_id BIGINT                           not null comment 'refers to user_id in store_api schema',
    status      VARCHAR(20)                      null comment 'for larger applications, should be in another table',
    created_at  DATETIME       default CURRENT_TIMESTAMP not null,
    total_price DECIMAL(10, 2) default 0         not null
);

create table order_items
(
    id          BIGINT auto_increment
        primary key,
    order_id    BIGINT                   not null,
    product_id  BIGINT                   not null,
    unit_price  DECIMAL(10, 2)           not null,
    quantity    int            default 1 not null,
    total_price DECIMAL(10, 2) default 0 not null,
    constraint fk_order_items_orders
        foreign key (order_id) references `orders` (id),
    constraint fk_order_items_products
        foreign key (product_id) references products (id)
);
