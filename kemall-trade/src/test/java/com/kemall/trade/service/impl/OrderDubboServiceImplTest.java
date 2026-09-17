package com.kemall.trade.service.impl;

import com.kemall.api.dto.OrderDto;
import com.kemall.api.dubbo.OrderDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class OrderDubboServiceImplTest {

    @DubboReference
    private OrderDubboService orderDubboService;

    @Test
    public void test(){
        OrderDto orderDto = orderDubboService.queryOrderBrief("OD202609171");
        System.out.println(orderDto);
    }

}