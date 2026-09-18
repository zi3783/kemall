package com.kemall.pay.domain.state.impl;

import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.domain.model.PaymentContext;
import com.kemall.pay.domain.state.PaymentStatus;

public class SuccessStatus implements PaymentStatus {
    @Override
    public PaymentStatusEnum getStatus() {
        return PaymentStatusEnum.SUCCESS;
    }

    @Override
    public void paySuccess(PaymentContext ctx) {
        ctx.changeStatus(PaymentStatusEnum.REFUNDING);
    }
}
