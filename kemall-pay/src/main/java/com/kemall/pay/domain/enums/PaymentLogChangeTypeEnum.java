package com.kemall.pay.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentLogChangeTypeEnum {

    INITIATED(1, "发起支付"),
    SUCCESS(2, "支付成功"),
    FAILED(3, "支付失败"),
    REFUNDED(4, "退款");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String desc;

}
