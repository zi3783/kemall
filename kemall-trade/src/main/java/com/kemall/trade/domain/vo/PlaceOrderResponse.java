package com.kemall.trade.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlaceOrderResponse {
    private Long orderId;
    private String orderNo;
    private Long totalAmount;
    private Long actualAmount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}