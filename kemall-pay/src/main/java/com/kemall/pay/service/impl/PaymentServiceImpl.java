package com.kemall.pay.service.impl;

import com.kemall.api.dubbo.AccountDubboService;
import com.kemall.pay.domain.po.Payment;
import com.kemall.pay.mapper.PaymentMapper;
import com.kemall.pay.service.IPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    private final

    @Override
    public void payment(Long orderNo, String channel, String requestId) {
        //查询订单

        //选择支付方式
        //内部账户支付
        //生成支付单插入数据库 （事务1）
        //调用账户服务扣款
        //（事务2开始）
        //保存支付单
        //保存确认扣减库存消息到本地消息队列
        //保存修改订单状态到本地消息队列
        //（事务2结束）
        //完成支付
        //异步扫描消息队列使用mq发送消息
    }
}
