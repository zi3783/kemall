package com.kemall.inventory.service;

import com.kemall.inventory.domain.po.Inventory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 库存表 服务类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
public interface IInventoryService extends IService<Inventory> {

    /**
     * TCC全局事务扣减库存入口：Try 阶段锁定库存，全局提交后由 TC 调用确认扣减
     *
     * @param skuId   商品SKU id
     * @param amount  扣减数量
     * @param orderNo 订单编号（幂等标识）
     * @return true-扣减成功
     */
    boolean deductByTcc(Long skuId, Integer amount, String orderNo);

}