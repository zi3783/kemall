package com.kemall.api.dubbo;

import com.kemall.api.domain.dto.OrderDto;

public interface OrderDubboService {
    OrderDto queryOrderBrief(String orderNo);
}
