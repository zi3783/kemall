package com.kemall.trade.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderTypeEnum {
    OX("OD", "普通订单"),
    RF("RF", "退款订单");
    private final String type;
    private final String desc;
}
