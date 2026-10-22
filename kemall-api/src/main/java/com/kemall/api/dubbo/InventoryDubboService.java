package com.kemall.api.dubbo;


import java.util.List;

public interface InventoryDubboService {

    boolean prepareDeductByTcc(Long skuId, Integer amount, String orderNo, boolean isSeckill);

    void prepareBatchDeductByTcc(List<Long> skuIds, List<Integer> quantities, String orderNo);
}
