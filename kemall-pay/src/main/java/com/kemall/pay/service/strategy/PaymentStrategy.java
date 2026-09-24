package com.kemall.pay.service.strategy;

import com.kemall.api.dto.OrderDto;
import com.kemall.pay.domain.enums.PaymentChannelEnum;

public interface PaymentStrategy {

    void pay(String paymentNo);

    PaymentChannelEnum getPaymentChannel();
}
