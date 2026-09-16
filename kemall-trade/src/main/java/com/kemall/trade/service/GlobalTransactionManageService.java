package com.kemall.trade.service;


import com.kemall.trade.domain.dto.OrderItemRequest;
import com.kemall.trade.domain.vo.OrderBrief;

import java.util.List;
import java.util.Map;

public interface GlobalTransactionManageService {

    /**
     * 预扣减库存并生成订单
     */
    OrderBrief deductStorageAndPlaceOrder(List<OrderItemRequest> cartItems, Map<Long, Long> priceMap, String idempotencyKey) throws Throwable;

}
