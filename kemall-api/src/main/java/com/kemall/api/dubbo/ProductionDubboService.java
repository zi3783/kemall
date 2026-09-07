package com.kemall.api.dubbo;


import com.kemall.api.result.Result;

public interface ProductionDubboService {
    boolean skuExist(Long productId,Long skuId);
    Result<Long> getSkuPrice(Long productId, Long skuId);
}
