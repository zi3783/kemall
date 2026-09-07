package com.kemall.product.service.impl;

import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.product.domain.vo.ProductSkuVO;
import com.kemall.product.domain.vo.ProductVO;
import com.kemall.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;

@DubboService
@Service
@RequiredArgsConstructor
public class ProductionDubboServiceImpl implements ProductionDubboService {

    private final IProductService productService;

    @Override
    public boolean skuExist(Long productId, Long skuId) {
        Result<ProductVO> result = productService.getProductDetail(productId);
        ProductVO pro = result.getData();
        List<ProductSkuVO> list = pro.getProductSkuVOList();
        for (ProductSkuVO sku : list) {
            if(sku.getId().equals(skuId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public com.kemall.api.result.Result<Long> getSkuPrice(Long productId, Long skuId){
        Result<ProductVO> result = productService.getProductDetail(productId);
        ProductVO pro = result.getData();
        List<ProductSkuVO> list = pro.getProductSkuVOList();
        for (ProductSkuVO sku : list) {
            if(sku.getId().equals(skuId)){
                return com.kemall.api.result.Result.success(sku.getPrice());
            }
        }
        return com.kemall.api.result.Result.fail("未找到sku");
    }
}
