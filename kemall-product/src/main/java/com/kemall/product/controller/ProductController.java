package com.kemall.product.controller;


import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.bean.result.PageResult;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.query.ProductQuery;
import com.kemall.product.domain.vo.ProductIntro;
import com.kemall.product.domain.vo.ProductVO;
import com.kemall.product.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@RestController
@RequestMapping("/products")
@Tag(name = "用户端商品相关接口")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @Operation(description = "查询商品详情")
    @GetMapping("/detail/{productId}")
    @LoginRequire(login = false)
    public Result<ProductVO> getProductDetail(@PathVariable Long productId) {
        return productService.getProductDetail(productId);
    }

    @Operation(description = "分页查询商品/商品页")
    @PostMapping("/list")
    @LoginRequire(login = false)
    public Result<PageResult<ProductIntro>> queryProductIntroByCondition(@RequestBody ProductQuery query){
        return productService.queryProductIntroByCondition(query);
    }
}
