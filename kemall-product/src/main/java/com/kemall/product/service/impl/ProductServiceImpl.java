package com.kemall.product.service.impl;

import com.kemall.product.domain.po.Product;
import com.kemall.product.mapper.ProductMapper;
import com.kemall.product.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

}
