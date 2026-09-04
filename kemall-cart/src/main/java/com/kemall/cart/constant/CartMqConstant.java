package com.kemall.cart.constant;



public class CartMqConstant {
    /**
     * 交换机名称
     */
    public static final String EXCHANGE_NAME = "cart.exchange";

    /**
     * 队列名称
     */
    public static final String QUEUE_NAME = "cart.queue";

    /**
     * 同步购物车路由键
     */
    public static final String ROUTING_KEY_SYNC = "cart.sync.key";

    /**
     * 备用交换机
     */
    public static final String BACKUP_EXCHANGE_NAME = "backup.exchange";

    /**
     * 备用队列
     */
    public static final String BACKUP_QUEUE_NAME = "backup.queue";
}
