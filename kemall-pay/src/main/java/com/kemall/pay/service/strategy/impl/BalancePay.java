package com.kemall.pay.service.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.api.constant.OrderMqConstant;
import com.kemall.api.dto.OrderNo;
import com.kemall.api.dto.WalletDTO;
import com.kemall.api.dubbo.AccountDubboService;
import com.kemall.api.enums.TransactionType;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.common.core.utils.UserContext;
import com.kemall.pay.constant.RedisConstant;
import com.kemall.pay.domain.enums.LocalMessageStatusEnum;
import com.kemall.pay.domain.enums.PaymentChannelEnum;
import com.kemall.pay.domain.enums.PaymentLogChangeTypeEnum;
import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.domain.po.LocalMessage;
import com.kemall.pay.domain.po.Payment;
import com.kemall.pay.domain.po.PaymentLog;
import com.kemall.pay.mapper.LocalMessageMapper;
import com.kemall.pay.mapper.PaymentLogMapper;
import com.kemall.pay.mapper.PaymentMapper;
import com.kemall.pay.service.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.tm.api.TransactionalExecutor;
import org.apache.seata.tm.api.TransactionalTemplate;
import org.apache.seata.tm.api.transaction.TransactionInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalancePay implements PaymentStrategy {

    private final RedissonClient redissonClient;

    private final PaymentMapper paymentMapper;

    private final AccountDubboService accountDubboService;

    private final TransactionTemplate transactionTemplate;

    private final PaymentLogMapper paymentLogMapper;

    private final TransactionalTemplate transactionalTemplate;

    private final LocalMessageMapper localMessageMapper;

    private final ObjectMapper objectMapper;


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

            LambdaUpdateWrapper<Payment> updateWrapper = new LambdaUpdateWrapper<Payment>()
                    .eq(Payment::getPaymentNo, paymentNo)
                    .eq(Payment::getStatus, PaymentStatusEnum.PENDING)
                    .set(Payment::getStatus, PaymentStatusEnum.SUCCESS)
                    .set(Payment::getPayTime, LocalDateTime.now());
            PaymentLog logger = PaymentLog.builder()
                    .paymentNo(paymentNo)
                    .orderNo(payment.getOrderNo())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .changeType(PaymentLogChangeTypeEnum.SUCCESS)
                    .beforeStatus(PaymentStatusEnum.PENDING)
                    .afterStatus(PaymentStatusEnum.SUCCESS)
                    .build();
            OrderNo orderNo = new OrderNo();
            orderNo.setOrderNo(payment.getOrderNo());
            LocalMessage localMessage = LocalMessage.builder()
                    .messageId(UUID.randomUUID().toString().replace("-", ""))
                    .exchange(OrderMqConstant.ORDER_STATE_EXCHANGE)
                    .routingKey(OrderMqConstant.ORDER_STATE_ROUTING_KEY)
                    .status(LocalMessageStatusEnum.PENDING)
                    .payload(objectMapper.writeValueAsString(orderNo))
                    .build();

            try {
                transactionalTemplate.execute(new TransactionalExecutor() {
                    @Override
                    public Object execute() throws Throwable {
                        accountDubboService.deductWallet(
                                WalletDTO.builder()
                                        .balance(payment.getAmount())
                                        .transactionType(TransactionType.CONSUME)
                                        .paymentNo(paymentNo)
                                        .build()
                        );
                        //写入数据库
                        transactionTemplate.executeWithoutResult(status -> {
                            int row = paymentMapper.update(updateWrapper);
                            if(row == 0) {
                                Payment latest = paymentMapper.selectByPaymentNo(paymentNo);
                                if (latest.getStatus() == PaymentStatusEnum.SUCCESS) {
                                    return;  // 幂等
                                }
                                throw new BusinessException("支付状态冲突");
                            }
                            paymentLogMapper.insert(logger);
                            //保存更改订单状态的消息到本地消息表
                            localMessageMapper.insert(localMessage);
                        });
                        return null;
                    }

                    @Override
                    public TransactionInfo getTransactionInfo() {
                        TransactionInfo info = new TransactionInfo();
                        info.setName("paymentTransaction");
                        info.setTimeOut(30000);
                        return info;
                    }
                });
            } catch (Throwable e) {
                log.error("全局事务系统错误");
                throw new RuntimeException(e);
            }
        } catch (JsonProcessingException e) {
            log.error("json序列化失败");
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public PaymentChannelEnum getPaymentChannel() {
        return PaymentChannelEnum.BALANCE;
    }
}
