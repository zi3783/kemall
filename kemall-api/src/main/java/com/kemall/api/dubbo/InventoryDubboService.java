package com.kemall.api.dubbo;

public interface InventoryDubboService {

    void deductByTcc(Long skuId, Integer amount, String orderNo);

}
