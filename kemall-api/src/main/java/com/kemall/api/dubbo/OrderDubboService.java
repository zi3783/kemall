package com.kemall.api.dubbo;

import com.kemall.api.dto.OrderDto;

public interface OrderDubboService {
    OrderDto queryOrderBrief(String orderNo);
}
