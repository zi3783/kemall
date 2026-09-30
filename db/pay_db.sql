-- Active: 1785938747882@@172.24.223.134@3306@pay_db
CREATE database pay_db;

CREATE TABLE `payment`
(
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `payment_no`      VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_no`        VARCHAR(64) NOT NULL COMMENT '关联订单号',
    `user_id`         BIGINT      NOT NULL COMMENT '用户ID',
    `amount`          BIGINT      NOT NULL COMMENT '支付金额（单位：分）',
    `channel`         VARCHAR(20) NOT NULL DEFAULT 'BALANCE' COMMENT '支付渠道 BALANCE-余额支付',
    `status`          TINYINT     NOT NULL DEFAULT 0 COMMENT '0-待支付 1-支付成功 2-支付失败 3-已关闭',
    `refunded_amount` BIGINT      NOT NULL DEFAULT 0 COMMENT '已退款金额（分）',
    `refund_status`   TINYINT     NOT NULL DEFAULT 0 COMMENT '0-未退款 1-部分退款 2-全额退款',
    `pay_time`        DATETIME             DEFAULT NULL COMMENT '支付完成时间',
    `fail_reason`     VARCHAR(255)         DEFAULT '' COMMENT '失败原因',
    `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '支付单表';

CREATE TABLE `payment_log`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `payment_no`    VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_no`      VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id`       BIGINT      NOT NULL COMMENT '用户ID',
    `amount`        BIGINT      NOT NULL COMMENT '金额（分）',
    `change_type`   TINYINT     NOT NULL COMMENT '1-发起支付 2-支付成功 3-支付失败 4-退款',
    `before_status` TINYINT     NOT NULL COMMENT '变更前状态',
    `after_status`  TINYINT     NOT NULL COMMENT '变更后状态',
    `remark`        VARCHAR(255)         DEFAULT '' COMMENT '备注',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '支付流水表';

CREATE TABLE `refund`
(
    `id`             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `biz_no`         VARCHAR(64) NOT NULL COMMENT '业务退款单号（幂等）',
    `refund_no`      VARCHAR(64) NOT NULL COMMENT '退款单号',
    `payment_no`     VARCHAR(64) NOT NULL COMMENT '关联支付单号',
    `order_no`       VARCHAR(64) NOT NULL COMMENT '关联订单号',
    `user_id`        BIGINT      NOT NULL COMMENT '用户ID',
    `amount`         BIGINT      NOT NULL COMMENT '退款金额（分）',
    `payment_amount` BIGINT      NOT NULL COMMENT '原始支付金额（分）',
    `reason`         VARCHAR(255)         DEFAULT '' COMMENT '退款原因',
    `status`         TINYINT     NOT NULL DEFAULT 0 COMMENT '0-待退款 1-退款成功 2-退款失败',
    `refund_time`    DATETIME             DEFAULT NULL COMMENT '退款完成时间',
    `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '退款表';

create table `local_message`
(
    `id`              bigint primary key auto_increment comment '主键',
    `message_id`      varchar(64)  not null comment '消息id',
    `exchange`        varchar(128) not null comment '交换机',
    `routing_key`     varchar(128) not null comment '路由键',
    `status`          tinyint      not null comment '0-未投递 1-已投递 2-失败',
    `payload`         text         not null comment '消息体json',
    `retry_count`     INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `max_retry`       INT          NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `next_retry_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次重试时间',
    `error_msg`       VARCHAR(512)          DEFAULT NULL COMMENT '最后一次失败原因',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    unique KEY `uk_message_id` (`message_id`),
    KEY `idx_status_next_retry` (`status`, `next_retry_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='本地消息表';


CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
