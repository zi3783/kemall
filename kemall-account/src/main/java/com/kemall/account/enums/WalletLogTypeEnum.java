package com.kemall.account.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WalletLogTypeEnum {
    RECHARGE(1, "充值"),
    WITHDRAW(2, "提现"),
    CONSUME(3, "消费"),
    REFUND(4, "退款"),
    SYSTEM_ADJUST(5,"系统调整");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String msg;

    WalletLogTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
