package com.kemall.trade.domain.vo;

import com.kemall.trade.enums.OrderStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderBrief {

    private Long id;
    private String orderNo;
    private Long actualAmount;
    private OrderStatusEnum status;
    private LocalDateTime expireTime;

}
