package com.kemall.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountStatusEnum {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结");

    @EnumValue
    private final int code;
    @JsonValue
    private final String desc;

    AccountStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
