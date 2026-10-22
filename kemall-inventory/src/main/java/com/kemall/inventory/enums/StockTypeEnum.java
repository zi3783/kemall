package com.kemall.inventory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StockTypeEnum {
    OD(1, "普通库存"),
    SK(2, "秒杀库存");

    private final Integer code;
    private final String desc;
}
