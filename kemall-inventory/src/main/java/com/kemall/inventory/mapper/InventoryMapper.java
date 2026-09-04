package com.kemall.inventory.mapper;

import com.kemall.inventory.domain.po.Inventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 库存表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * TCC Try：锁定库存 available_quantity -= amount，locked_quantity += amount（version 乐观锁）
     */
    Integer lockQuantity(@Param("skuId") Long skuId, @Param("amount") Integer amount, @Param("version") Integer version);

    /**
     * TCC Confirm：确认扣减 locked_quantity -= amount（version 乐观锁）
     */
    Integer deductLockedQuantity(@Param("skuId") Long skuId, @Param("amount") Integer amount, @Param("version") Integer version);

    /**
     * TCC Cancel：释放库存 locked_quantity -= amount，available_quantity += amount（version 乐观锁）
     */
    Integer releaseLockedQuantity(@Param("skuId") Long skuId, @Param("amount") Integer amount, @Param("version") Integer version);
}