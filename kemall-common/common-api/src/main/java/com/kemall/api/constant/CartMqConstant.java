package com.kemall.api.constant;

public class CartMqConstant {
    // ========== 数据同步（Redis → MySQL）==========
    public static final String SYNC_EXCHANGE_NAME = "cart.sync.exchange";
    public static final String SYNC_QUEUE_NAME = "cart.sync.queue";
    public static final String ROUTING_KEY_SYNC = "cart.sync.key";

    // 同步备用交换机（1:1 专属）
    public static final String SYNC_BACKUP_EXCHANGE = "cart.sync.backup.exchange";
    public static final String SYNC_BACKUP_QUEUE = "cart.sync.backup.queue";

    // ========== 清空购物车（独立）==========
    public static final String CLEAN_EXCHANGE_NAME = "cart.clean.exchange";
    public static final String CLEAN_QUEUE_NAME = "cart.clean.queue";
    public static final String ROUTING_KEY_CLEAN = "cart.clean.key";

    // 清空备用交换机（1:1 专属）
    public static final String CLEAN_BACKUP_EXCHANGE = "cart.clean.backup.exchange";
    public static final String CLEAN_BACKUP_QUEUE = "cart.clean.backup.queue";
}