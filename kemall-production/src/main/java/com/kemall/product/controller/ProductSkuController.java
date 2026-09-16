package com.kemall.product.controller;


import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.vo.SkuVo;
import com.kemall.product.service.IProductSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@RestController
@RequestMapping("/product-sku")
@Tag(name = "sku相关接口")
@RequiredArgsConstructor
public class ProductSkuController {

    private final IProductSkuService productSkuService;

    @GetMapping("/{spuId}")
    @Operation(summary = "根据spuId查询sku列表项")
    public Result<List<SkuVo>> querySkyBySpuId(@PathVariable Long spuId){
        return Result.success(productSkuService.queryBySpuId(spuId));
    }
}
