package com.kemall.pay.service;

import com.kemall.pay.domain.enums.PaymentChannelEnum;
import com.kemall.pay.domain.po.Payment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 支付单表 服务类
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
public interface IPaymentService extends IService<Payment> {

    void payment(String orderNo, PaymentChannelEnum channel, String requestId);
}
