package com.kemall.product.controller.admin;

import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.query.ProductCreateReq;
import com.kemall.product.domain.query.ProductUpdateReq;
import com.kemall.product.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class ProductAdminController {

    private final IProductService productService;

    @PostMapping
    @Operation(summary = "新增商品")
    @LoginRequire(login = false)
    public Result<Void> create(@RequestBody @Valid ProductCreateReq req) {
        productService.createProduct(req);
        return Result.success();
    }

    @Operation(summary = "上架商品")
    @PutMapping("/{id}/status")
    @LoginRequire(login = false)
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    @LoginRequire(login = false)
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ProductUpdateReq req) {
        req.setId(id);
        productService.updateProduct(req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除,使用status字段代替")
    @LoginRequire(login = false)
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}
