-- =============================================================================
-- 消费者身份表（customer）迁移脚本
-- 与 tenant_member / platform_member 平级：同一全局账号可兼具多重身份。
-- =============================================================================

USE iam_db;

CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '全局账号 ID（sys_user.id）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 active 2 disabled',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_customer_user (user_id),
    KEY idx_customer_status (status)
) ENGINE = InnoDB COMMENT = '消费者身份';
