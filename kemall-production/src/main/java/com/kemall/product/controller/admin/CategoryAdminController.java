package com.kemall.product.controller.admin;

import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.dto.CategoryDTO;
import com.kemall.product.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@Tag(name = "管理端分类相关接口")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final ICategoryService categoryService;

    @Operation(summary = "新增分类")
    @PostMapping
    @LoginRequire(login = false)
    public Result<String> saveCategory(@RequestBody CategoryDTO dto){
        categoryService.saveCategory(dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{categoryId}")
    @LoginRequire(login = false)
    public Result<String> deleteCategory(@PathVariable Long categoryId){
        categoryService.deleteCategory(categoryId);
        return Result.success();
    }

}
