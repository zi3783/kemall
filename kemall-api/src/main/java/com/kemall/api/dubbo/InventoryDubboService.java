package com.kemall.api.dubbo;

public interface InventoryDubboService {

    boolean prepareDeductByTcc(Long skuId, Integer amount, String orderNo);

}
