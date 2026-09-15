package com.kemall.inventory.service.impl.dubbo;

import com.kemall.api.dubbo.InventoryDubboService;
import com.kemall.inventory.service.IInventoryTccService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Slf4j
@RequiredArgsConstructor
public class InventoryDubboServiceImpl implements InventoryDubboService {

    private final IInventoryTccService inventoryTccService;

    @Override
    public boolean prepareDeductByTcc(Long skuId, Integer amount, String orderNo) {
        return inventoryTccService.prepareFreeze(skuId,amount,orderNo);
    }
}
