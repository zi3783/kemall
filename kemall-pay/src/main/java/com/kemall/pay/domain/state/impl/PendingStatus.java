package com.kemall.pay.domain.state.impl;

import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.domain.model.PaymentContext;
import com.kemall.pay.domain.state.PaymentStatus;

public class PendingStatus implements com.kemall.pay.domain.state.PaymentStatus {
    @Override
    public PaymentStatusEnum getStatus() {
        return  PaymentStatusEnum.PENDING;
    }

    @Override
    public void paySuccess(PaymentContext ctx) {
        ctx.changeStatus(PaymentStatusEnum.SUCCESS);
    }
}
