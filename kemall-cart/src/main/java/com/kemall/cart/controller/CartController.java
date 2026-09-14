package com.kemall.cart.controller;


import com.kemall.cart.service.ICartService;
import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.bean.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 购物车主表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-09-04
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "购物车相关接口")
public class CartController {

    private final ICartService cartService;

    private final RabbitTemplate rabbitTemplate;

    @Operation(description = "新增商品到购物车")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<String> addToCart(Long productId,Long skuId, Integer quantity){
        cartService.addToCart(productId, skuId, quantity);
        return Result.success();
    }


//    @Operation(description = "清空购物车")
//    @PutMapping("/clear")
//    public Result<String> clearCart(){
//        //发送mq
//        rabbitTemplate.convertAndSend(
//                CartMqConstant.CLEAN_EXCHANGE_NAME,
//                CartMqConstant.ROUTING_KEY_CLEAN,
//                UserContext.getUserId(),
//                new CorrelationData(UUID.randomUUID().toString())
//        );
//        return Result.success();
//    }

    @Operation(description = "查询用户购物车列表")
    @GetMapping("/user/list")
    public Result<Map<String, Object>> cartListByUserId(Long userId){
        return Result.success(cartService.listCart(userId));
    }
}
