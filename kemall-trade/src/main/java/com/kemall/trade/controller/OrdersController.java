package com.kemall.trade.controller;


import com.kemall.common.utils.bean.result.Result;
import com.kemall.trade.domain.dto.OrderRequest;
import com.kemall.trade.domain.vo.PlaceOrderResponse;
import com.kemall.trade.service.IOrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-09-14
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final IOrdersService ordersService;

    @PostMapping
    public Result<Object> placeOrder(@RequestBody OrderRequest request){
        return Result.success(ordersService.placeOrder(request));
    }
}
