package com.kemall.inventory.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StockTypeEnum {
    OD(1, "普通库存"),
    SK(2, "秒杀库存");


    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;
}
