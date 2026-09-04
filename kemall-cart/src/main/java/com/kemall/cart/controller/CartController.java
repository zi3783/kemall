package com.kemall.cart.controller;


import com.kemall.cart.service.ICartService;
import com.kemall.common.utils.bean.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    private final ICartService iCartService;

    @Operation(description = "新增商品到购物车")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<String> addToCart(Long skuId, Integer quantity){
        iCartService.addToCart(skuId, quantity);
        return Result.success();
    }
}
