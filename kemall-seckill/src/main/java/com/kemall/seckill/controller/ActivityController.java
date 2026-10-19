package com.kemall.seckill.controller;


import com.kemall.common.core.utils.bean.result.Result;
import com.kemall.seckill.service.IActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2026-10-19
 */
@RestController
@RequestMapping("/activity")
@Tag(name = "秒杀活动相关接口")
@RequiredArgsConstructor
public class ActivityController {

    private final IActivityService activityService;

    @Operation(description = "秒杀入口")
    @GetMapping("/seckill")
    public Result<String> seckill(Long skuId){
        activityService.seckill(skuId);
        return Result.success();
    }
}
