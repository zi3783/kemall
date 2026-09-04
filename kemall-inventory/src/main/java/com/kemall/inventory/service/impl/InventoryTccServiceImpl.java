package com.kemall.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kemall.common.annotation.RedissonLock;
import com.kemall.common.exception.BusinessException;
import com.kemall.inventory.domain.po.Inventory;
import com.kemall.inventory.domain.po.InventoryLog;
import com.kemall.inventory.enums.InventoryChangeTypeEnum;
import com.kemall.inventory.mapper.InventoryLogMapper;
import com.kemall.inventory.mapper.InventoryMapper;
import com.kemall.inventory.service.IInventoryTccService;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 库存 Seata TCC 扣减实现
 * </p>
 * <p>
 * 幂等设计（基于 inventory_log 的 order_no + change_type 状态机 + CAS）：
 * <li>Try：已存在释放(3)记录则不再锁定（防悬挂）；已存在锁定(1)/扣减(2)记录则幂等返回</li>
 * <li>Confirm：状态已是扣减(2)幂等返回；CAS 将锁定(1) -> 扣减(2)，保证只扣一次</li>
 * <li>Cancel：无记录时插入释放(3)记录（空回滚，同时防悬挂）；状态已是释放(3)幂等返回；
 * CAS 将锁定(1) -> 释放(3)，保证只释放一次；已扣减(2)不允许回滚</li>
 * </p>
 *
 * @author author
 * @since 2026-08-31
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryTccServiceImpl implements IInventoryTccService {

    private final InventoryMapper inventoryMapper;

    private final InventoryLogMapper inventoryLogMapper;

    // ==================== TCC Try：锁定库存 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#skuId", waitTime = 3, prefix = "Inventory:SkuId:Lock:")
    public boolean prepareDeduct(BusinessActionContext actionContext, Long skuId, Integer amount, String orderNo) {
        if (skuId == null || amount == null || amount <= 0 || orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("TCC锁定库存参数错误");
        }
        //幂等/防悬挂检查
        InventoryLog stateLog = getLatestLog(orderNo);
        if (stateLog != null) {
            if (stateLog.getChangeType().equals(InventoryChangeTypeEnum.RELEASE.getCode())) {
                //rollback先于try到达（空回滚），防悬挂：不再锁定
                log.info("TCC锁定库存：orderNo={} 已空回滚，不再锁定", orderNo);
                return false;
            }
            //已经锁定或已经扣减，幂等返回成功
            log.info("TCC锁定库存：orderNo={} changeType={}，幂等返回", orderNo, stateLog.getChangeType());
            return true;
        }
        //查询库存
        Inventory inventory = inventoryMapper.selectById(skuId);
        if (inventory == null) {
            throw new BusinessException("库存不存在");
        }
        if (inventory.getAvailableQuantity() < amount) {
            throw new BusinessException("库存不足");
        }
        //锁定：available -= amount，locked += amount（version 乐观锁）
        int row = inventoryMapper.lockQuantity(skuId, amount, inventory.getVersion());
        if (row != 1) {
            throw new BusinessException("锁定库存失败，版本号错误");
        }
        //记录流水（锁定）
        inventoryLogMapper.insert(new InventoryLog()
                .setSkuId(skuId)
                .setOrderNo(orderNo)
                .setChangeType(InventoryChangeTypeEnum.LOCK.getCode())
                .setChangeAmount(amount)
                .setBeforeAvailable(inventory.getAvailableQuantity())
                .setAfterAvailable(inventory.getAvailableQuantity() - amount)
                .setBeforeLocked(inventory.getLockedQuantity())
                .setAfterLocked(inventory.getLockedQuantity() + amount));
        log.info("TCC锁定库存成功：orderNo={} skuId={} amount={}", orderNo, skuId, amount);
        return true;
    }

    // ==================== TCC Confirm：确认扣减 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean commitDeduct(BusinessActionContext actionContext) {
        String orderNo = getStringFromContext(actionContext, "orderNo");
        if (orderNo == null) {
            throw new BusinessException("TCC上下文参数缺失，无法确认扣减");
        }
        InventoryLog stateLog = getLatestLog(orderNo);
        if (stateLog == null) {
            throw new BusinessException("锁定记录不存在，无法确认扣减");
        }
        //幂等：已确认扣减直接返回
        if (stateLog.getChangeType().equals(InventoryChangeTypeEnum.DEDUCT.getCode())) {
            log.info("TCC确认：orderNo={} 已确认扣减，幂等返回", orderNo);
            return true;
        }
        if (stateLog.getChangeType().equals(InventoryChangeTypeEnum.RELEASE.getCode())) {
            throw new BusinessException("库存已释放，无法确认扣减");
        }
        //CAS：锁定(1) -> 扣减(2)，保证只扣一次
        int row = inventoryLogMapper.updateChangeType(
                InventoryChangeTypeEnum.DEDUCT.getCode(),
                InventoryChangeTypeEnum.LOCK.getCode(),
                stateLog.getId());
        if (row != 1) {
            InventoryLog latest = inventoryLogMapper.selectById(stateLog.getId());
            if (latest != null && latest.getChangeType().equals(InventoryChangeTypeEnum.DEDUCT.getCode())) {
                //并发下已被确认，幂等返回
                return true;
            }
            throw new BusinessException("确认扣减失败，锁定记录状态冲突");
        }
        //扣减锁定库存：locked -= amount（version 乐观锁）
        Long skuId = stateLog.getSkuId();
        Integer amount = stateLog.getChangeAmount();
        Inventory inventory = inventoryMapper.selectById(skuId);
        if (inventory == null) {
            throw new BusinessException("库存不存在");
        }
        row = inventoryMapper.deductLockedQuantity(skuId, amount, inventory.getVersion());
        if (row != 1) {
            throw new BusinessException("确认扣减失败，库存版本冲突");
        }
        //记录流水（扣减）
        inventoryLogMapper.insert(new InventoryLog()
                .setSkuId(skuId)
                .setOrderNo(orderNo)
                .setChangeType(InventoryChangeTypeEnum.DEDUCT.getCode())
                .setChangeAmount(amount)
                .setBeforeAvailable(inventory.getAvailableQuantity())
                .setAfterAvailable(inventory.getAvailableQuantity())
                .setBeforeLocked(inventory.getLockedQuantity())
                .setAfterLocked(inventory.getLockedQuantity() - amount));
        log.info("TCC确认扣减成功：orderNo={} skuId={} amount={}", orderNo, skuId, amount);
        return true;
    }

    // ==================== TCC Cancel：取消扣减 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackDeduct(BusinessActionContext actionContext) {
        Long skuId = getLongFromContext(actionContext, "skuId");
        Integer amount = getIntFromContext(actionContext, "amount");
        String orderNo = getStringFromContext(actionContext, "orderNo");
        if (skuId == null || amount == null || orderNo == null) {
            throw new BusinessException("TCC上下文参数缺失，无法释放库存");
        }
        InventoryLog stateLog = getLatestLog(orderNo);
        if (stateLog == null) {
            //空回滚：try未执行，插入释放(3)记录防悬挂
            Inventory inventory = inventoryMapper.selectById(skuId);
            int available = inventory == null ? 0 : inventory.getAvailableQuantity();
            int locked = inventory == null ? 0 : inventory.getLockedQuantity();
            inventoryLogMapper.insert(new InventoryLog()
                    .setSkuId(skuId)
                    .setOrderNo(orderNo)
                    .setChangeType(InventoryChangeTypeEnum.RELEASE.getCode())
                    .setChangeAmount(amount)
                    .setBeforeAvailable(available)
                    .setAfterAvailable(available)
                    .setBeforeLocked(locked)
                    .setAfterLocked(locked));
            log.info("TCC空回滚：orderNo={} 已记录释放记录防悬挂", orderNo);
            return true;
        }
        //幂等：已释放直接返回
        if (stateLog.getChangeType().equals(InventoryChangeTypeEnum.RELEASE.getCode())) {
            log.info("TCC取消：orderNo={} 已释放，幂等返回", orderNo);
            return true;
        }
        if (stateLog.getChangeType().equals(InventoryChangeTypeEnum.DEDUCT.getCode())) {
            //已确认扣减，不允许回滚
            throw new BusinessException("已确认扣减，无法释放库存");
        }
        //CAS：锁定(1) -> 释放(3)，保证只释放一次
        int row = inventoryLogMapper.updateChangeType(
                InventoryChangeTypeEnum.RELEASE.getCode(),
                InventoryChangeTypeEnum.LOCK.getCode(),
                stateLog.getId());
        if (row != 1) {
            InventoryLog latest = inventoryLogMapper.selectById(stateLog.getId());
            if (latest != null) {
                if (latest.getChangeType().equals(InventoryChangeTypeEnum.RELEASE.getCode())) {
                    return true;
                }
                if (latest.getChangeType().equals(InventoryChangeTypeEnum.DEDUCT.getCode())) {
                    throw new BusinessException("已确认扣减，无法释放库存");
                }
            }
            throw new BusinessException("释放库存失败，锁定记录状态冲突");
        }
        //释放：locked -= amount，available += amount（version 乐观锁）
        Long stateSkuId = stateLog.getSkuId();
        Integer stateAmount = stateLog.getChangeAmount();
        Inventory inventory = inventoryMapper.selectById(stateSkuId);
        if (inventory == null) {
            throw new BusinessException("库存不存在");
        }
        row = inventoryMapper.releaseLockedQuantity(stateSkuId, stateAmount, inventory.getVersion());
        if (row != 1) {
            throw new BusinessException("释放库存失败，库存版本冲突");
        }
        //记录流水（释放）
        inventoryLogMapper.insert(new InventoryLog()
                .setSkuId(stateSkuId)
                .setOrderNo(orderNo)
                .setChangeType(InventoryChangeTypeEnum.RELEASE.getCode())
                .setChangeAmount(stateAmount)
                .setBeforeAvailable(inventory.getAvailableQuantity())
                .setAfterAvailable(inventory.getAvailableQuantity() + stateAmount)
                .setBeforeLocked(inventory.getLockedQuantity())
                .setAfterLocked(inventory.getLockedQuantity() - stateAmount));
        log.info("TCC释放库存成功：orderNo={} skuId={} amount={}", orderNo, stateSkuId, stateAmount);
        return true;
    }

    // ==================== 工具方法 ====================

    /**
     * 按订单号查询流水（取最新一条保证幂等判断稳定）
     */
    private InventoryLog getLatestLog(String orderNo) {
        return inventoryLogMapper.selectOne(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getOrderNo, orderNo)
                .orderByDesc(InventoryLog::getId)
                .last("LIMIT 1"));
    }

    /**
     * 从事务上下文读取 Long 值（上下文经过序列化，数值类型可能是 Integer/Long，统一 toString 转换）
     */
    private Long getLongFromContext(BusinessActionContext context, String key) {
        Object value = context.getActionContext(key);
        return value == null ? null : Long.valueOf(value.toString());
    }

    /**
     * 从事务上下文读取 Integer 值
     */
    private Integer getIntFromContext(BusinessActionContext context, String key) {
        Object value = context.getActionContext(key);
        return value == null ? null : Integer.valueOf(value.toString());
    }

    /**
     * 从事务上下文读取 String 值
     */
    private String getStringFromContext(BusinessActionContext context, String key) {
        Object value = context.getActionContext(key);
        return value == null ? null : value.toString();
    }
}