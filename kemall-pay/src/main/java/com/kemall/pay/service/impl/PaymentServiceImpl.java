package com.kemall.pay.service.impl;

import com.kemall.api.dto.OrderDto;
import com.kemall.api.dubbo.AccountDubboService;
import com.kemall.api.dubbo.OrderDubboService;
import com.kemall.common.exception.BusinessException;
import com.kemall.pay.domain.enums.PaymentChannelEnum;
import com.kemall.pay.domain.po.Payment;
import com.kemall.pay.mapper.PaymentMapper;
import com.kemall.pay.service.IPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.pay.service.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付单表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements IPaymentService {

    @DubboReference
    private final AccountDubboService accountDubboService;

    @DubboReference(timeout = 5000, check = false)
    private final OrderDubboService orderDubboService;

    private final PaymentStrategy  paymentStrategy;

    //todo 支付接口未完成，仍有很多bug
    @Override
    public void payment(String orderNo, PaymentChannelEnum channel, String requestId) {
        //查询订单
        OrderDto orderBrief = orderDubboService.queryOrderBrief(orderNo);
        if(orderBrief == null) {
            throw new BusinessException("为找到订单");
        }
        //选择支付方式
        paymentStrategy.pay(orderBrief);
        //异步扫描消息队列使用mq发送消息
    }
}
