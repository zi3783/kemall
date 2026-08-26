package com.kemall.inventory.service.impl;

import com.kemall.inventory.domain.po.Inventory;
import com.kemall.inventory.mapper.InventoryMapper;
import com.kemall.inventory.service.IInventoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements IInventoryService {

}
