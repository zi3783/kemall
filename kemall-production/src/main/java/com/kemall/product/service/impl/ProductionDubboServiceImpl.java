package com.kemall.product.service.impl;

import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.vo.ProductVO;
import com.kemall.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@DubboService
@Service
@RequiredArgsConstructor
public class ProductionDubboServiceImpl implements ProductionDubboService {

    private final IProductService productService;

    @Override
    public boolean skuExist(Long productId) {
        Result<ProductVO> result = productService.getProductDetail(productId);
        ProductVO pro = result.getData();
        //todo 查询商品sku是否存在
    }
}
