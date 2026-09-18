package com.kemall.pay.domain.state.impl;

import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.domain.model.PaymentContext;
import com.kemall.pay.domain.state.PaymentStatus;

public class ClosedStatus implements PaymentStatus {


    @Override
    public PaymentStatusEnum getStatus() {
        return PaymentStatusEnum.CLOSED;
    }

}
