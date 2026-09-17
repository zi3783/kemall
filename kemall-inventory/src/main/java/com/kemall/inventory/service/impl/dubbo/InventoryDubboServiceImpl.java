package com.kemall.inventory.service.impl.dubbo;

import com.kemall.api.dubbo.InventoryDubboService;
import com.kemall.inventory.service.IInventoryTccService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.seata.core.context.RootContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@DubboService
@Slf4j
@RequiredArgsConstructor
public class InventoryDubboServiceImpl implements InventoryDubboService {

    private final IInventoryTccService inventoryTccService;

    private final ThreadPoolExecutor tccDeductInventoryThreadPool;

    @Override
    public boolean prepareDeductByTcc(Long skuId, Integer amount, String orderNo) {
        return inventoryTccService.prepareFreeze(skuId, amount, orderNo);
    }

    @Override
    public void prepareBatchDeductByTcc(List<Long> skuIds, List<Integer> quantities, String orderNo) {
        String xid = RootContext.getXID();
        List<CompletableFuture<Boolean>> futureList = new ArrayList<>(skuIds.size());
        for (int i = 0; i < skuIds.size(); i++) {
            Long skuId = skuIds.get(i);
            Integer amount = quantities.get(i);
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                if (xid == null) {
                    throw new RuntimeException("全局事务 XID 为空，无法执行库存 TCC 调用");
                }
                RootContext.bind(xid);
                try {
                    boolean result = inventoryTccService.prepareFreeze(skuId, amount, orderNo);
                    if (!result) {
                        throw new RuntimeException("库存预扣失败, skuId=" + skuId);
                    }
                    return true;
                } finally {
                    RootContext.unbind();
                }
            }, tccDeductInventoryThreadPool);
            futureList.add(future);
        }
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Boolean> future : futureList) {
                if (!future.get()) {
                    throw new RuntimeException("异步调用库存服务扣减库存异常");
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("tcc分支回滚：xid：" + xid, e);
        }
    }
}
