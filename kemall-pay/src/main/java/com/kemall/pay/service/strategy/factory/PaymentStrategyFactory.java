package com.kemall.pay.service.strategy.factory;

import com.kemall.common.core.exception.BusinessException;
import com.kemall.pay.domain.enums.PaymentChannelEnum;
import com.kemall.pay.service.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentChannelEnum, PaymentStrategy> strategyMap;

    public PaymentStrategyFactory(List<PaymentStrategy> paymentStrategies) {
        strategyMap = new EnumMap<>(PaymentChannelEnum.class);
        paymentStrategies.forEach(ps -> strategyMap.put(ps.getPaymentChannel(), ps));
    }


    public PaymentStrategy getStrategy(PaymentChannelEnum channel) {
        PaymentStrategy strategy = strategyMap.get(channel);
        if (strategy == null) {
            throw new BusinessException("不支持的支付渠道: " + channel);
        }
        return strategy;
    }
}