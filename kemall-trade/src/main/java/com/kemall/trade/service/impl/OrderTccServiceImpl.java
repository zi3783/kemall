package com.kemall.trade.service.impl;

import com.kemall.trade.domain.dto.OrderItemRequest;
import com.kemall.trade.domain.po.OrderItems;
import com.kemall.trade.domain.po.Orders;
import com.kemall.trade.enums.OrderStatusEnum;
import com.kemall.trade.mapper.OrderItemsMapper;
import com.kemall.trade.mapper.OrdersMapper;
import com.kemall.trade.service.OrderTccService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.rm.tcc.api.BusinessActionContext;
import org.apache.seata.rm.tcc.api.BusinessActionContextParameter;
import org.apache.seata.rm.tcc.api.LocalTCC;
import org.apache.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@LocalTCC
@Slf4j
public class OrderTccServiceImpl implements OrderTccService {

    private final OrdersMapper ordersMapper;

    private final OrderItemsMapper orderItemsMapper;

    @TwoPhaseBusinessAction(
            name = "createOrder",
            commitMethod = "confirmCreate",
            rollbackMethod = "cancelCreate",
            useTCCFence = true
    )
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long tryCreateOrder(@BusinessActionContextParameter(paramName = "orderNo") String orderNo,
                               Long userId,
                               String idempotencyKey,
                               LocalDateTime expireTime,
                               Long totalAmount,
                               List<OrderItemRequest> items,
                               Map<Long, Long> priceMap) {
        // 本地事务包住订单和明细的插入
        Orders order = Orders.builder()
                .orderNo(orderNo)
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .totalAmount(totalAmount)
                .actualAmount(totalAmount)
                .status(OrderStatusEnum.PENDING)
                .expireTime(expireTime)
                .build();
        ordersMapper.insert(order);

        List<OrderItems> orderItems = items.stream().map(item -> OrderItems.builder()
                .orderId(order.getId())
                .skuId(item.getSkuId())
                .skuPrice(priceMap.get(item.getSkuId()))
                .productQuantity(item.getQuantity())
                .build()).toList();

        int row = orderItemsMapper.insertBatch(orderItems);
        if(row != items.size()){
            log.error("插入的数据与实际数据数目不一致");
            throw new RuntimeException("插入的数据与实际数据数目不一致");
        }
        return order.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmCreate(BusinessActionContext context) {
        String orderNo = Objects.requireNonNull(context.getActionContext("orderNo")).toString();
        int row = ordersMapper.updateStatusByOrderNo(orderNo, OrderStatusEnum.WAIT_PAY);
        if(row != 1){
            log.error("未找到订单或者状态机错误");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelCreate(BusinessActionContext context) {
        String orderNo = Objects.requireNonNull(context.getActionContext("orderNo")).toString();
        ordersMapper.deleteByOrderNo(orderNo);
        orderItemsMapper.deleteByOrderNo(orderNo);
    }
}