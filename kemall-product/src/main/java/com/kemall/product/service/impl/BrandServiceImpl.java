package com.kemall.product.service.impl;

import com.kemall.product.domain.po.Brand;
import com.kemall.product.mapper.BrandMapper;
import com.kemall.product.service.IBrandService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品品牌表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements IBrandService {

}
