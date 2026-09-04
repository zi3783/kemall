-- Active: 1785938747882@@172.24.223.134@3306@trade_db
create DATABASE trade_db;

use trade_db;

create table orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单id',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户id',
    total_amount BIGINT NOT null COMMENT '订单总金额',
    actual_amount BIGINT NOT null COMMENT '实际支付金额',
    `status` TINYINT not null COMMENT '订单状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE index idx_order_no (order_no),
    index idx_user_id (user_id),
    index idx_create_time (create_time)
) COMMENT '订单表';

create table order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项id',
    order_id BIGINT NOT NULL COMMENT '订单id',
    product_id BIGINT NOT NULL COMMENT '商品id',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_price BIGINT NOT NULL COMMENT '商品价格',
    product_quantity INT NOT NULL COMMENT '商品数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
)COMMENT '订单项表'