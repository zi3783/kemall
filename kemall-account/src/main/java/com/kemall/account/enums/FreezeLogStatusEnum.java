package com.kemall.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FreezeLogStatusEnum {
    TRY(1, "尝试扣款"),
    CONFIRM(2, "已经扣款"),
    CANCEL(3, "取消扣款");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String msg;

    private FreezeLogStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
