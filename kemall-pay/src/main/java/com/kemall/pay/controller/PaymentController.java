package com.kemall.pay.controller;


import com.kemall.common.utils.bean.result.Result;
import com.kemall.pay.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 支付单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-09-17
 */
@RestController
@RequestMapping("/payment")
@Tag(name = "支付相关接口")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @GetMapping
    @Operation(summary = "发起支付")
    public Result<String> payment(Long orderNo, String channel, String requestId){
        paymentService.payment(orderNo, channel, requestId);
        return Result.success();
    }


}
