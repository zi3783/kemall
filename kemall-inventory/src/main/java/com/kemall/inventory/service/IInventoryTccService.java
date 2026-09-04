package com.kemall.inventory.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * <p>
 * 库存 Seata TCC 扣减接口
 * </p>
 * <p>
 * TCC 三阶段：
 * <li>Try（prepareDeduct）：锁定库存，available_quantity 转入 locked_quantity，写入 inventory_log（change_type=1 锁定）</li>
 * <li>Confirm（commitDeduct）：确认扣减，扣减 locked_quantity，inventory_log 状态 1 -> 2</li>
 * <li>Cancel（rollbackDeduct）：取消扣减，锁定库存退回 available_quantity，inventory_log 状态 1 -> 3</li>
 * </p>
 *
 * @author author
 * @since 2026-08-31
 */
@LocalTCC
public interface IInventoryTccService {

    /**
     * TCC Try：锁定库存
     *
     * @param actionContext 事务上下文（本地调用传 null，由 Seata 拦截器注入）
     * @param skuId         商品SKU id
     * @param amount        锁定数量
     * @param orderNo       订单编号（幂等标识）
     * @return true-锁定成功 false-锁定失败（触发全局回滚）
     */
    @TwoPhaseBusinessAction(name = "inventoryTccDeduct", commitMethod = "commitDeduct", rollbackMethod = "rollbackDeduct")
    boolean prepareDeduct(BusinessActionContext actionContext,
                          @BusinessActionContextParameter(paramName = "skuId") Long skuId,
                          @BusinessActionContextParameter(paramName = "amount") Integer amount,
                          @BusinessActionContextParameter(paramName = "orderNo") String orderNo);

    /**
     * TCC Confirm：确认扣减（由 TC 调用，需幂等）
     *
     * @param actionContext 事务上下文
     * @return true-确认成功
     */
    boolean commitDeduct(BusinessActionContext actionContext);

    /**
     * TCC Cancel：取消扣减（由 TC 调用，需幂等、支持空回滚）
     *
     * @param actionContext 事务上下文
     * @return true-取消成功
     */
    boolean rollbackDeduct(BusinessActionContext actionContext);
}