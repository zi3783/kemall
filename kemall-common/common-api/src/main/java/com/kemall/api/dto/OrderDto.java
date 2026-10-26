package com.kemall.api.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@ToString
@Data
public class OrderDto implements Serializable {
    /**
     * 订单id
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 幂等键
     */
    private String idempotencyKey;

    /**
     * 订单总金额
     */
    private Long totalAmount;

    /**
     * 实际支付金额
     */
    private Long actualAmount;

    /**
     * 订单状态 1:待付款 2:待发货 3:待收货 4:待评价 5:已完成 6:已取消
     */
    private Integer status;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;

}
