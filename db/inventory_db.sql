-- Active: 1785938747882@@172.24.223.134@3306@inventory_db
create DATABASE inventory_db;

use inventory_db;


CREATE Table inventory (
    sku_id BIGINT PRIMARY KEY COMMENT '商品SKU id',
    available_quantity INT NOT NULL COMMENT '可用库存数量',
    locked_quantity INT NOT NULL COMMENT '锁定库存数量',

    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新时间'
)COMMENT '库存表';


create TABLE inventory_log(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存日志id',
    sku_id BIGINT NOT NULL COMMENT '商品SKU id',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    change_type TINYINT NOT NULL COMMENT '1-锁定 2-扣减 3-释放 4-入库',
    change_amount INT NOT NULL,
    before_available INT NOT NULL,
    after_available INT NOT NULL,
    before_locked INT NOT NULL,
    after_locked INT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    index idx_sku_id (sku_id),
    index idx_order_id (order_no)
) comment '库存流水表';
