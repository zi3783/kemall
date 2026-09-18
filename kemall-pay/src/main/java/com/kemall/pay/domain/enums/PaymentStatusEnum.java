package com.kemall.pay.domain.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentStatusEnum {
    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败"),
    CLOSED(3, "已关闭"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "已退款");
    @EnumValue
    private final Integer code;
    private final String desc;
}
