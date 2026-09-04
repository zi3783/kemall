package com.kemall.inventory.service.impl;

import com.kemall.common.exception.BusinessException;
import com.kemall.inventory.domain.po.Inventory;
import com.kemall.inventory.mapper.InventoryMapper;
import com.kemall.inventory.service.IInventoryService;
import com.kemall.inventory.service.IInventoryTccService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements IInventoryService {

    private final IInventoryTccService inventoryTccService;

    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "inventory-tcc-deduct")
    public boolean deductByTcc(Long skuId, Integer amount, String orderNo) {
        boolean prepare = inventoryTccService.prepareDeduct(skuId, amount, orderNo);
        if (!prepare) {
            throw new BusinessException("TCC锁定库存失败");
        }
        return true;
    }
}