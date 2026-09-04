package com.kemall.inventory.mapper;

import com.kemall.inventory.domain.po.InventoryLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 库存流水表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {

    /**
     * TCC 状态 CAS：change_type 从 oldType 变为 newType，保证 Confirm/Cancel 只生效一次
     */
    Integer updateChangeType(@Param("newType") Integer newType, @Param("oldType") Integer oldType, @Param("id") Long id);
}