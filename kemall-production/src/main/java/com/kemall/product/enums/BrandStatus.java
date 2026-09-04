package com.kemall.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BrandStatus {
    NORMAL(1, "正常"),
    DISABLED(2, "禁用");

    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String desc;

    BrandStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}