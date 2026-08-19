package com.kemall.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountStatusEnum {
    NORMAL(1,"正常"),
    FROZEN(2,"冻结");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String msg;
    AccountStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
