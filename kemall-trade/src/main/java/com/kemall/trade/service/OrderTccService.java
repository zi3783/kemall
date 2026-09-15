package com.kemall.trade.service;

import com.kemall.trade.domain.dto.OrderItemRequest;
import org.apache.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface OrderTccService {
    @Transactional(rollbackFor = Exception.class)
    boolean tryCreateOrder(String orderNo, Long userId, String idempotencyKey,
                        Long totalAmount, List<OrderItemRequest> items,
                        Map<Long, Long> priceMap);
}
