package com.kemall.inventory.service.impl;

import com.kemall.inventory.domain.po.InventoryLog;
import com.kemall.inventory.mapper.InventoryLogMapper;
import com.kemall.inventory.service.IInventoryLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存流水表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-26
 */
@Service
public class InventoryLogServiceImpl extends ServiceImpl<InventoryLogMapper, InventoryLog> implements IInventoryLogService {

}
