package com.kemall.pay.controller;


import com.kemall.common.core.utils.bean.result.Result;
import com.kemall.pay.domain.enums.PaymentChannelEnum;
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

//    //todo 需要重新设计
//    @GetMapping
//    @Operation(summary = "发起支付")
//    public Result<String> payment(String orderNo, String channelStr, String requestId){
//        paymentService.payment(orderNo, PaymentChannelEnum.fromString(channelStr),requestId);
//        return Result.success();
//    }

    @GetMapping("/create")
    @Operation(summary = "生成支付单")
    public Result<String> createPayment(String orderNo){
        String paymentNo = paymentService.createPayment(orderNo);
        return Result.success(paymentNo);
    }

    @GetMapping("/execute")
    @Operation(summary = "执行扣款")
    public Result<String> executeDeduct(String paymentNo, String channelStr){
        paymentService.executeDeduct(paymentNo, PaymentChannelEnum.fromString(channelStr));
        return Result.success();
    }
}
