package com.kemall.product.controller;


import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.vo.CategoryTreeVO;
import com.kemall.product.service.ICategoryService;
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
 * 商品分类表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "用户端分类相关接口")
public class CategoryController {

    private final ICategoryService categoryService;

    @Operation(summary = "查询分类树")
    @GetMapping("/tree")
    @LoginRequire(login = false)
    public Result<List<CategoryTreeVO>> getChildrenCategory(){
        return categoryService.getCategoryTree();
    }
}
