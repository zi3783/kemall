package com.kemall.inventory.controller;


import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.inventory.service.IInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 库存表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@RestController
@RequestMapping("/inventory")
@Tag(name = "库存相关接口")
@RequiredArgsConstructor
@MapperScan("com.kemall.inventory.mapper")
public class InventoryController {

    private final IInventoryService inventoryService;

    @PostMapping("/tcc/deduct")
    @LoginRequire(login = false)
    @Operation(summary = "TCC事务扣减库存（Try锁定，全局提交后确认扣减）")
    public Result<String> deductByTcc(Long skuId, Integer amount, String orderNo) {
        return inventoryService.deductByTcc(skuId, amount, orderNo) ? Result.success() : Result.fail("扣减失败");
    }
}