package com.kemall.account.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * <p>
 * 账户 Seata TCC 扣款接口
 * </p>
 * <p>
 * TCC 三阶段：
 * <li>Try（prepareDeduct）：冻结金额，balance 转入 frozen_balance，写入 freeze_log（状态 TRY）</li>
 * <li>Confirm（commitDeduct）：确认扣款，扣减 frozen_balance，freeze_log 状态 TRY -> CONFIRM</li>
 * <li>Cancel（rollbackDeduct）：取消扣款，冻结金额退回 balance，freeze_log 状态 TRY -> CANCEL</li>
 * </p>
 *
 * @author author
 * @since 2026-08-31
 */
@LocalTCC
public interface IAccountTccService {

    /**
     * TCC Try：冻结金额
     *
     * @param actionContext 事务上下文（本地调用传 null，由 Seata 拦截器注入）
     * @param userId        用户id
     * @param amount        扣款金额（分）
     * @param bizId         业务id（幂等标识）
     * @return true-冻结成功 false-冻结失败（触发全局回滚）
     */
    @TwoPhaseBusinessAction(name = "accountTccDeduct", commitMethod = "commitDeduct", rollbackMethod = "rollbackDeduct")
    boolean prepareDeduct(BusinessActionContext actionContext,
                          @BusinessActionContextParameter(paramName = "userId") Long userId,
                          @BusinessActionContextParameter(paramName = "amount") Long amount,
                          @BusinessActionContextParameter(paramName = "bizId") String bizId);

    /**
     * TCC Confirm：确认扣款（由 TC 调用，需幂等）
     *
     * @param actionContext 事务上下文
     * @return true-确认成功
     */
    boolean commitDeduct(BusinessActionContext actionContext);

    /**
     * TCC Cancel：取消扣款（由 TC 调用，需幂等、支持空回滚）
     *
     * @param actionContext 事务上下文
     * @return true-取消成功
     */
    boolean rollbackDeduct(BusinessActionContext actionContext);
}