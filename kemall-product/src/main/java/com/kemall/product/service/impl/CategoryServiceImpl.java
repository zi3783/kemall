package com.kemall.product.service.impl;

import com.kemall.product.domain.po.Category;
import com.kemall.product.mapper.CategoryMapper;
import com.kemall.product.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品分类表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
