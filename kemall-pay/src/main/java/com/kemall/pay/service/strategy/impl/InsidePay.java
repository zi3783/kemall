package com.kemall.pay.service.strategy.impl;

import com.kemall.api.dto.OrderDto;
import com.kemall.api.dto.WalletDTO;
import com.kemall.api.dubbo.AccountDubboService;
import com.kemall.api.enums.TransactionType;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import com.kemall.pay.domain.model.PaymentContext;
import com.kemall.pay.domain.po.Payment;
import com.kemall.pay.domain.po.PaymentLog;
import com.kemall.pay.domain.enums.PaymentChannelEnum;
import com.kemall.pay.domain.enums.PaymentLogChangeTypeEnum;
import com.kemall.pay.domain.enums.PaymentStatusEnum;
import com.kemall.pay.mapper.PaymentLogMapper;
import com.kemall.pay.mapper.PaymentMapper;
import com.kemall.pay.service.strategy.PaymentStrategy;
import com.kemall.pay.util.PaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsidePay implements PaymentStrategy {

    @DubboReference(timeout = 5000, check = false)
    private final AccountDubboService accountDubboService;

    private final TransactionTemplate transactionTemplate;

    private final PaymentUtil paymentUtil;

    private final PaymentMapper paymentMapper;

    private final PaymentLogMapper paymentLogMapper;

    //todo 基本功能没完成，并发问题没处理
    @Override
    public void pay(OrderDto order) {
        Long userId = UserContext.getUserId();
        String orderNo = order.getOrderNo();
        //内部账户支付
        //生成支付单插入数据库 （事务1）
        Payment payment = Payment.builder().paymentNo(paymentUtil.generateOrderNo(PaymentChannelEnum.BALANCE))
                .orderNo(order.getOrderNo())
                .userId(userId)
                .amount(order.getActualAmount())
                .channel(PaymentChannelEnum.BALANCE.getChannel())
                .status(PaymentStatusEnum.PENDING)
                .build();
        try {
            paymentMapper.insert(payment);
        } catch (DuplicateKeyException e) {
            log.debug("用户支付单已经存在");
            payment = paymentMapper.selectByOrderNo(orderNo);
            if(payment.getStatus() == PaymentStatusEnum.SUCCESS){
                throw new BusinessException("支付单已经支付");
            }else if(payment.getStatus() == PaymentStatusEnum.FAILED){
                throw new RuntimeException("支付单状态异常");
            }
        }
        PaymentContext paymentContext = new PaymentContext();
        paymentContext.setStatusEnum(payment.getStatus());
        //调用账户服务扣款
        try{
            accountDubboService.deductWallet(
                    WalletDTO.builder()
                            .userId(userId)
                            .balance(order.getActualAmount())
                            .transactionType(TransactionType.CONSUME)
                            .paymentNo(payment.getPaymentNo())
                            .build()
            );
        }catch (Exception e){
            log.debug("扣款失败");
            Payment finalPayment = payment;
            transactionTemplate.executeWithoutResult(status -> {
                paymentMapper.updateStatus(finalPayment.getId(), PaymentStatusEnum.FAILED.getCode());
                paymentLogMapper.insert(
                        PaymentLog.builder().paymentNo(finalPayment.getPaymentNo())
                                .orderNo(finalPayment.getOrderNo())
                                .userId(userId)
                                .amount(finalPayment.getAmount())
                                .changeType(PaymentLogChangeTypeEnum.FAILED)
                                .beforeStatus(PaymentStatusEnum.PENDING)
                                .afterStatus(PaymentStatusEnum.FAILED)
                                .remark("支付失败")
                                .build());
            });
            throw new BusinessException("扣款失败" + e.getMessage());
        }
        //（事务2开始）
        Payment finalPayment = payment;
        transactionTemplate.executeWithoutResult(status -> {
            //修改支付单和日志
            paymentContext.paySuccess();
            finalPayment.setStatus(paymentContext.getStatusEnum());
            int row = paymentMapper.updateStatus(finalPayment.getId(), PaymentStatusEnum.SUCCESS.getCode());
            if(row == 0){
                throw new RuntimeException("支付状态错误");
            }
            paymentLogMapper.insert(
                    PaymentLog.builder().paymentNo(finalPayment.getPaymentNo())
                            .orderNo(finalPayment.getOrderNo())
                            .userId(userId)
                            .amount(finalPayment.getAmount())
                            .changeType(PaymentLogChangeTypeEnum.SUCCESS)
                            .beforeStatus(PaymentStatusEnum.PENDING)
                            .afterStatus(PaymentStatusEnum.SUCCESS)
                            .remark("")
                            .build()
            );
            //todo 基本功能没完成！！！
            //保存确认扣减库存消息到本地消息队列
            //保存修改订单状态到本地消息队列
        });
        //（事务2结束）
        //完成支付
    }

    @Override
    public PaymentChannelEnum getPaymentChannel() {
        return PaymentChannelEnum.BALANCE;
    }
}
