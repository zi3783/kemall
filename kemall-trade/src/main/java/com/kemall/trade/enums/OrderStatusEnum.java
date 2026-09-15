package com.kemall.trade.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatusEnum {
    /**
     * 订单状态枚举
     * 1:待付款 2:待发货 3:待收货 4:待评价 5:已完成 6:已取消
     */
    WAIT_PAY(1, "待付款"),
    WAIT_SHIP(2, "待发货"),
    WAIT_RECEIVE(3, "待收货"),
    WAIT_EVALUATE(4, "待评价"),
    COMPLETED(5, "已完成"),
    CANCELED(6, "已取消"),
    PENDING(0, "待创建");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;

}
