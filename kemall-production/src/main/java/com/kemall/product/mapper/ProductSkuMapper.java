package com.kemall.product.mapper;

import com.kemall.product.domain.po.ProductSku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    List<ProductSku> selectBySpuId(Long spuId);
}
