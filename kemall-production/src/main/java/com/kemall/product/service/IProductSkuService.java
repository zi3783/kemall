package com.kemall.product.service;

import com.kemall.product.domain.po.ProductSku;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
public interface IProductSkuService extends IService<ProductSku> {

    Map<Long, Long> getSkuPriceByIds(List<Long> skuIds);
}
