package com.kemall.trade.service.impl;

import com.kemall.api.dto.OrderDto;
import com.kemall.api.dubbo.OrderDubboService;
import com.kemall.trade.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@DubboService
@Component
@RequiredArgsConstructor
public class OrderDubboServiceImpl implements OrderDubboService {

    private final OrdersMapper ordersMapper;

    @Override
    public OrderDto queryOrderBrief(String orderNo) {
        return ordersMapper.selectByOrderNo(orderNo);
    }

}
