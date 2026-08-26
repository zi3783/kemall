package com.kemall.trade.controller;


import com.kemall.common.utils.bean.result.Result;
import com.kemall.trade.service.IOrdersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "订单相关接口")
@RequiredArgsConstructor
public class OrdersController {

    private final IOrdersService ordersService;

    public Result<String> generateOrder(){
//        return ordersService.generateOrder() ? Result.success() : Result.fail();
        return null;
    }

}
