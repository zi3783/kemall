package com.kemall.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoryStatus {
    NORMAL(1, "正常"),
    DISABLED(2, "禁用");

    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String desc;

    CategoryStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
}