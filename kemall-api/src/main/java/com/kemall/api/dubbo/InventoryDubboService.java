package com.kemall.api.dubbo;


import java.util.List;

public interface InventoryDubboService {

    boolean prepareDeductByTcc(Long skuId, Integer amount, String orderNo);

    void prepareBatchDeductByTcc(List<Long> skuIds, List<Integer> quantities, String orderNo);
}
