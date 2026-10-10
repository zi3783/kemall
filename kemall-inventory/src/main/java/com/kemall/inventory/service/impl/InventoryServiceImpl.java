package com.kemall.inventory.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.common.core.exception.BusinessException;
import com.kemall.inventory.constant.RedisConstant;
import com.kemall.inventory.domain.po.Inventory;
import com.kemall.inventory.domain.po.InventoryLog;
import com.kemall.inventory.enums.InventoryChangeTypeEnum;
import com.kemall.inventory.mapper.InventoryLogMapper;
import com.kemall.inventory.mapper.InventoryMapper;
import com.kemall.inventory.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private final ObjectMapper objectMapper;

    private final InventoryLogMapper inventoryLogMapper;

    private final RedissonClient redissonClient;

    private final TransactionTemplate transactionTemplate;

    private final InventoryMapper inventoryMapper;

    @Override
    public boolean releaseInventory(String json) {
        try{
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<>() {});
            if(list.isEmpty()){
                log.info("库存释放列表为null");
                return false;
            }
            String orderNo = (String) list.get(0).get("order_no");

            RLock lock = redissonClient.getLock(RedisConstant.INVENTORY_RELEASE_LOCK_PREFIX + orderNo);
            boolean success = false;
            try {
                success = lock.tryLock(5, -1, TimeUnit.SECONDS);
                if(!success){
                    return false;
                }
                //检查log有没有执行过

                boolean exists = new LambdaQueryChainWrapper<>(inventoryLogMapper).eq(InventoryLog::getOrderNo, orderNo)
                        .eq(InventoryLog::getChangeType, InventoryChangeTypeEnum.RELEASE)
                        .exists();
                if(exists){
                    log.info("已经释放库存：{}", orderNo);
                    return true;
                }

                List<InventoryLog> logs = new ArrayList<>();

                for (Map<String, Object> log : list) {
                    long skuId = ((Number) log.get("sku_id")).longValue();
                    InventoryLog l = InventoryLog.builder().skuId(skuId)
                            .orderNo(orderNo)
                            .changeType(InventoryChangeTypeEnum.RELEASE)
                            .changeAmount((Integer) log.get("product_quantity")).build();
                    logs.add(l);
                }

                transactionTemplate.executeWithoutResult(status -> {
                    inventoryMapper.releaseInventory(list);
                    inventoryLogMapper.insert(logs);
                });
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally {
                if (success && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("jsonToObject is fail");
        }
    }

//    @Override
//    @GlobalTransactional(timeoutMills = 300000, name = "inventory-tcc-deduct")
//    public boolean prepareDeductByTcc(Long skuId, Integer amount, String orderNo) {
//        boolean prepare = inventoryTccService.prepareDeduct(skuId, amount, orderNo);
//        if (!prepare) {
//            throw new BusinessException("TCC锁定库存失败");
//        }
//        return true;
//    }
}