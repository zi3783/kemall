package com.kemall.pay.service.impl;

import com.kemall.pay.domain.po.PaymentLog;
import com.kemall.pay.mapper.PaymentLogMapper;
import com.kemall.pay.service.IPaymentLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付流水表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
@Service
public class PaymentLogServiceImpl extends ServiceImpl<PaymentLogMapper, PaymentLog> implements IPaymentLogService {

}
