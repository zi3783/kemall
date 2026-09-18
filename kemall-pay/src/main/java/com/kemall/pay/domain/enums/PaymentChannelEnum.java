package com.kemall.pay.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PaymentChannelEnum {
    BALANCE("BALANCE"),;

    @JsonValue
    private final String channel;
    PaymentChannelEnum(String channel) {
        this.channel = channel;
    }

    public static PaymentChannelEnum fromString(String channel) {
        if("BALANCE".equalsIgnoreCase(channel)) {
            return BALANCE;
        }
        throw new IllegalArgumentException("参数不是枚举类型对应的字符串");
    }

}
