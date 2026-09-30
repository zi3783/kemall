package com.kemall.pay.service.impl;

import com.kemall.api.dto.OrderDto;
import com.kemall.api.dubbo.AccountDubboService;
import com.kemall.api.dubbo.OrderDubboService;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.common.core.utils.UserContext;
import com.kemall.pay.constant.RedisConstant;
import com.kemall.pay.domain.enums.PaymentChannelEnum;
import com.kemall.pay.domain.enums.PaymentLogChangeTypeEnum;
import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.domain.po.Payment;
import com.kemall.pay.domain.po.PaymentLog;
import com.kemall.pay.mapper.PaymentLogMapper;
import com.kemall.pay.mapper.PaymentMapper;
import com.kemall.pay.service.IPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.pay.service.strategy.PaymentStrategy;
import com.kemall.pay.service.strategy.factory.PaymentStrategyFactory;
import com.kemall.pay.util.PaymentUtil;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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

    private final TransactionTemplate transactionTemplate;
    @DubboReference
    private AccountDubboService accountDubboService;

    @DubboReference(timeout = 5000, check = false)
    private OrderDubboService orderDubboService;

    private final PaymentUtil paymentUtil;

    private final RedissonClient redissonClient;

    private final PaymentLogMapper paymentLogMapper;

    private final PaymentStrategyFactory paymentStrategyFactory;

    //todo 支付接口未完成，仍有很多bug
    @Override
    public void payment(String orderNo, PaymentChannelEnum channel, String requestId) {
        //查询订单
        OrderDto orderBrief = orderDubboService.queryOrderBrief(orderNo);
        if(orderBrief == null) {
            throw new BusinessException("为找到订单");
        }
        //选择支付方式
//        paymentStrategy.pay(orderBrief);
        //异步扫描消息队列使用mq发送消息
    }

    @Override
    public String createPayment(String orderNo) {
        Long userId = UserContext.getUserId();
        RLock lock = redissonClient.getLock(RedisConstant.PAYMENT_ORDER_LOCK + orderNo);
        boolean success = lock.tryLock();
        if (!success) {
            throw new BusinessException("支付正在进行");
        }
        try{
            //查找数据库是否已经存在订单处于未支付状态
            Payment one = lambdaQuery().eq(Payment::getOrderNo, orderNo)
                    .eq(Payment::getStatus, PaymentStatusEnum.PENDING)
                    .one();
            if(one != null) {
                return one.getPaymentNo();
            }

            //查询订单
            OrderDto orderBrief = orderDubboService.queryOrderBrief(orderNo);
            if (orderBrief == null) {
                throw new BusinessException("未找到订单");
            }

            String paymentNo = paymentUtil.generateOrderNo();

            transactionTemplate.executeWithoutResult(tx -> {
                Payment payment = Payment.builder().paymentNo(paymentNo)
                        .userId(userId)
                        .amount(orderBrief.getActualAmount())
                        .orderNo(orderNo)
                        .status(PaymentStatusEnum.PENDING)
                        .build();
                PaymentLog log = PaymentLog.builder().paymentNo(paymentNo)
                        .orderNo(orderNo)
                        .userId(userId)
                        .amount(orderBrief.getActualAmount())
                        .changeType(PaymentLogChangeTypeEnum.INITIATED)
                        .beforeStatus(PaymentStatusEnum.PENDING)
                        .afterStatus(PaymentStatusEnum.PENDING).build();
                save(payment);
                paymentLogMapper.insert(log);
            });
            return paymentNo;
        }catch (RuntimeException e){
            throw new RuntimeException("发起支付失败", e);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void executeDeduct(String paymentNo, PaymentChannelEnum paymentChannelEnum) {
        //选择支付渠道
        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(paymentChannelEnum);
        strategy.pay(paymentNo);
    }
}
