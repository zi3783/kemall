package com.kemall.product.service.impl.dubbo;

import com.kemall.api.dubbo.ProductionDubboService;
import com.kemall.product.service.IProductService;
import com.kemall.product.service.IProductSkuService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@DubboService
@Service
@RequiredArgsConstructor
public class ProductionDubboServiceImpl implements ProductionDubboService {


    private final IProductSkuService productSkuService;

    @Override
    public Map<Long, Long> getSkuPriceByIds(List<Long> skuIds){
        return productSkuService.getSkuPriceByIds(skuIds);
    }
}
