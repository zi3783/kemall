package com.kemall.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WalletLogTypeEnum {
    RECHARGE(1, "充值"),
    DEDUCT(2,"扣款");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    WalletLogTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
