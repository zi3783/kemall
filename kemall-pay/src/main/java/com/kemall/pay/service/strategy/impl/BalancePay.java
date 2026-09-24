package com.kemall.pay.service.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kemall.api.dto.WalletDTO;
import com.kemall.api.dubbo.AccountDubboService;
import com.kemall.api.enums.TransactionType;
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
import com.kemall.pay.service.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalancePay implements PaymentStrategy {

    private final RedissonClient redissonClient;

    private final PaymentMapper paymentMapper;

    private final AccountDubboService accountDubboService;

    private final TransactionTemplate transactionTemplate;

    private final PaymentLogMapper paymentLogMapper;

    @Override
    public void pay(String paymentNo) {
        Long userId = UserContext.getUserId();

        RLock lock = redissonClient.getLock(RedisConstant.PAYMENT_PAYMENT_NO_LOCK + paymentNo);
        boolean success = lock.tryLock();
        if (!success) {
            throw new BusinessException("正在支付中");
        }
        try {
            //先查看支付单号是否存在
            LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getPaymentNo, paymentNo)
                    .eq(Payment::getUserId, userId);
            List<Payment> payments = paymentMapper.selectList(wrapper);
            if(payments.isEmpty()){
                throw new BusinessException("支付单不属于该用户");
            }
            if(payments.size() > 1){
                throw new BusinessException("支付单数据异常！");
            }
            Payment payment = payments.get(0);

            if(payment.getStatus().equals(PaymentStatusEnum.SUCCESS)){
                log.info("已经支付");
                return ;
            }
            accountDubboService.deductWallet(
                    WalletDTO.builder()
                            .balance(payment.getAmount())
                            .transactionType(TransactionType.CONSUME)
                            .paymentNo(paymentNo)
                            .build()
            );

            LambdaUpdateWrapper<Payment> updateWrapper = new LambdaUpdateWrapper<Payment>()
                    .eq(Payment::getPaymentNo, paymentNo)
                    .set(Payment::getStatus, PaymentStatusEnum.SUCCESS);
            PaymentLog log = PaymentLog.builder()
                    .paymentNo(paymentNo)
                    .orderNo(payment.getOrderNo())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .changeType(PaymentLogChangeTypeEnum.SUCCESS)
                    .beforeStatus(PaymentStatusEnum.PENDING)
                    .afterStatus(PaymentStatusEnum.SUCCESS)
                    .build();
            transactionTemplate.executeWithoutResult(status -> {
                paymentMapper.update(updateWrapper);
                paymentLogMapper.insert(log);
            });

            //todo 这里开始
        }finally {
            lock.unlock();
        }

    }

    @Override
    public PaymentChannelEnum getPaymentChannel() {
        return PaymentChannelEnum.BALANCE;
    }
}
