package com.kemall.pay.domain.state;

import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.domain.model.PaymentContext;

public interface PaymentStatus {
    PaymentStatusEnum getStatus();

    default void paySuccess(PaymentContext ctx) {
        throw new IllegalStateException("当前状态不允许支付成功");
    }
    default void payFail(PaymentContext ctx) {
        throw new IllegalStateException("当前状态不允许支付失败");
    }
    default void refund(PaymentContext ctx) {
        throw new IllegalStateException("当前状态不允许退款");
    }
    default void refundSuccess(PaymentContext ctx) {
        throw new IllegalStateException("当前状态不允许退款成功");
    }
    default void refundFail(PaymentContext ctx) {
        throw new IllegalStateException("当前状态不允许退款失败");
    }
    default void close(PaymentContext ctx) {
        throw new IllegalStateException("当前状态不允许关闭");
    }
}
