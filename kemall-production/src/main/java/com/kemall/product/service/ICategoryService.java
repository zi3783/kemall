package com.kemall.product.service;

import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.dto.CategoryDTO;
import com.kemall.product.domain.po.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kemall.product.domain.vo.CategoryTreeVO;

import java.util.List;

/**
 * <p>
 * 商品分类表 服务类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
public interface ICategoryService extends IService<Category> {

    Result<List<CategoryTreeVO>> getCategoryTree();

    void saveCategory(CategoryDTO dto);

    void deleteCategory(Long categoryId);
}
