package com.kemall.trade.service.impl;


import com.kemall.api.dubbo.InventoryDubboService;
import com.kemall.common.utils.UserContext;
import com.kemall.trade.domain.dto.OrderItemRequest;
import com.kemall.trade.domain.vo.OrderBrief;
import com.kemall.trade.enums.OrderStatusEnum;
import com.kemall.trade.enums.OrderTypeEnum;
import com.kemall.trade.service.GlobalTransactionManageService;
import com.kemall.trade.service.OrderTccService;
import com.kemall.trade.util.OrderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.tm.api.TransactionalExecutor;
import org.apache.seata.tm.api.TransactionalTemplate;
import org.apache.seata.tm.api.transaction.Propagation;
import org.apache.seata.tm.api.transaction.RollbackRule;
import org.apache.seata.tm.api.transaction.TransactionInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalTransactionManageServiceImpl implements GlobalTransactionManageService {

    private static final TransactionalTemplate TEMPLATE = new TransactionalTemplate();

    @DubboReference
    private final InventoryDubboService inventoryDubboService;

    private final OrderUtil orderUtil;

    private final ThreadPoolExecutor placeOrderThreadPool;

    private final OrderTccService orderTccService;


    @Override
    public OrderBrief deductStorageAndPlaceOrder(List<OrderItemRequest> cartItems, Map<Long, Long> priceMap, String idempotencyKey) throws Throwable {
        try {
            return (OrderBrief) TEMPLATE.execute(new TransactionalExecutor() {
                //生成订单编号
                @Override
                public Object execute() throws Throwable {
                    String orderNo = orderUtil.generateOrderNo(OrderTypeEnum.OX);
                    String xid = RootContext.getXID();
                    List<CompletableFuture<Boolean>> futureList = cartItems.stream().map(item -> {
                        Long skuId = item.getSkuId();
                        Integer amount = item.getQuantity();
                        return CompletableFuture.supplyAsync(() -> {
                            if (xid == null) {
                                throw new RuntimeException("全局事务 XID 为空，无法执行库存 TCC 调用");
                            }
                            RootContext.bind(xid);
                            try {
                                boolean result = inventoryDubboService.prepareDeductByTcc(skuId, amount, orderNo);
                                if (!result) {
                                    throw new RuntimeException("库存预扣失败, skuId=" + skuId);
                                }
                                return true;
                            } finally {
                                RootContext.unbind();
                            }
                        }, placeOrderThreadPool);
                    }).toList();

                    for (CompletableFuture<Boolean> future : futureList) {
                        if(!future.get()){
                            throw new RuntimeException("异步调用库存服务扣减库存异常");
                        }
                    }

                    long totalAmount = cartItems.stream()
                            .mapToLong(item -> priceMap.get(item.getSkuId()) * item.getQuantity())
                            .sum();
                    LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30);
                    Long orderId = orderTccService.tryCreateOrder(
                            orderNo,
                            UserContext.getUserId(),
                            idempotencyKey,
                            expireTime,
                            totalAmount,
                            cartItems,
                            priceMap
                    );


                    OrderBrief orderBrief = new OrderBrief();
                    orderBrief.setOrderNo(orderNo);
                    orderBrief.setId(orderId);
                    orderBrief.setStatus(OrderStatusEnum.WAIT_PAY);
                    orderBrief.setActualAmount(totalAmount);
                    orderBrief.setExpireTime(expireTime);

                    return orderBrief;
                }

                @Override
                public TransactionInfo getTransactionInfo() {
                    TransactionInfo info = new TransactionInfo();

                    // 超时时间
                    info.setTimeOut(30000);

                    // 事务名称，用于日志和监控
                    info.setName("createOrder");

                    // 传播行为
                    info.setPropagation(Propagation.REQUIRED);

                    // 回滚规则。不设置的话，默认所有异常都回滚
                    Set<RollbackRule> rollbackRules = new LinkedHashSet<>();
                    info.setRollbackRules(rollbackRules);

                    // 锁重试配置，TCC 模式下影响较小
                    info.setLockRetryInterval(10);
                    info.setLockRetryTimes(30);

                    return info;
                }
            });
        } catch (TransactionalExecutor.ExecutionException e) {
            log.error("全局事务异常, name=createOrder, xid={}", RootContext.getXID(), e);
            throw e;
        }
    }

}
