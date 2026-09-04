package com.kemall.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kemall.common.annotation.RedissonLock;
import com.kemall.account.domain.po.FreezeLog;
import com.kemall.account.domain.po.Wallet;
import com.kemall.account.domain.po.WalletLog;
import com.kemall.account.enums.AccountStatusEnum;
import com.kemall.account.enums.FreezeLogStatusEnum;
import com.kemall.account.enums.WalletLogTypeEnum;
import com.kemall.account.mapper.FreezeLogMapper;
import com.kemall.account.mapper.WalletLogMapper;
import com.kemall.account.mapper.WalletMapper;
import com.kemall.account.service.IAccountTccService;
import com.kemall.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.rm.tcc.api.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * <p>
 * 账户 Seata TCC 扣款实现
 * </p>
 * <p>
 * 幂等设计（基于 freeze_log 的 biz_id + status + version 乐观锁）：
 * <li>Try：已存在 CANCEL 记录则不再冻结（防悬挂）；已存在 TRY/CONFIRM 记录则幂等返回</li>
 * <li>Confirm：status 已是 CONFIRM 幂等返回；CAS 将 TRY -> CONFIRM，保证只扣一次</li>
 * <li>Cancel：无记录时插入 CANCEL 记录（空回滚，同时防悬挂）；status 已是 CANCEL 幂等返回；
 * CAS 将 TRY -> CANCEL，保证只退款一次；已 CONFIRM 不允许回滚</li>
 * </p>
 *
 * @author author
 * @since 2026-08-31
 */
@Service
@Slf4j
@RequiredArgsConstructor
@LocalTCC
public class AccountTccServiceImpl implements IAccountTccService {

    private final FreezeLogMapper freezeLogMapper;

    private final WalletMapper walletMapper;

    private final WalletLogMapper walletLogMapper;

    // ==================== TCC Try：冻结金额 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#userId", waitTime = 3, prefix = "Account:UserId:Lock:")
    @TwoPhaseBusinessAction(name = "accountTccDeduct", commitMethod = "commitDeduct", rollbackMethod = "rollbackDeduct")
    public boolean prepareDeduct(@BusinessActionContextParameter(paramName = "userId") Long userId,
                                 @BusinessActionContextParameter(paramName = "amount") Long amount,
                                 @BusinessActionContextParameter(paramName = "bizId") String bizId) {
        BusinessActionContext actionContext = BusinessActionContextUtil.getContext();
        log.info("TCC参数已保存: userId={}, amount={}, bizId={}", userId, amount, bizId);
        if (userId == null || amount == null || amount <= 0 || bizId == null || bizId.isBlank()) {
            throw new IllegalArgumentException("TCC冻结参数错误");
        }
//        // 保存参数到上下文，供 commit/rollback 使用
//        actionContext.getActionContext().put("userId", userId);
//        actionContext.getActionContext().put("amount", amount);
//        actionContext.getActionContext().put("bizId", bizId);
        //幂等/防悬挂检查
        FreezeLog freezeLog = getFreezeLog(bizId);
        if (freezeLog != null) {
            if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CANCEL)) {
                //rollback先于try到达（空回滚），防悬挂：不再冻结
                log.info("TCC冻结：bizId={} 已空回滚，不再冻结", bizId);
                return false;
            }
            //已经冻结或已经扣款，幂等返回成功
            log.info("TCC冻结：bizId={} 状态={}，幂等返回", bizId, freezeLog.getStatus());
            return true;
        }
        //查询钱包
        Wallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("用户不存在");
        }
        if (wallet.getStatus().equals(AccountStatusEnum.FROZEN)) {
            throw new BusinessException("账户被冻结");
        }
        if (wallet.getBalance() < amount) {
            throw new BusinessException("余额不足");
        }
        //冻结：balance -= amount，frozen_balance += amount（乐观锁）
        int row = walletMapper.freezeBalance(userId, amount, wallet.getVersion());
        if (row != 1) {
            throw new BusinessException("冻结失败，版本号错误");
        }
        //记录冻结日志（TRY）
        freezeLogMapper.insert(new FreezeLog()
                .setUserId(userId)
                .setAmount(amount)
                .setBizId(bizId)
                .setStatus(FreezeLogStatusEnum.TRY));
        log.info("TCC冻结成功：bizId={} userId={} amount={}", bizId, userId, amount);
        return true;
    }

    // ==================== TCC Confirm：确认扣款 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean commitDeduct(BusinessActionContext actionContext) {
        log.debug("commitDeduct调用");
        Long userId = getLongFromContext(actionContext, "userId");
        String bizId = getStringFromContext(actionContext, "bizId");
        if (userId == null || bizId == null) {
            throw new BusinessException("TCC上下文参数缺失，无法确认扣款");
        }
        FreezeLog freezeLog = getFreezeLog(bizId);
        if (freezeLog == null) {
            throw new BusinessException("冻结记录不存在，无法确认扣款");
        }
        //幂等：已确认扣款直接返回
        if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CONFIRM)) {
            log.info("TCC确认：bizId={} 已确认扣款，幂等返回", bizId);
            return true;
        }
        if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CANCEL)) {
            throw new BusinessException("冻结已取消，无法确认扣款");
        }
        //CAS：TRY -> CONFIRM，保证只扣一次
        int row = freezeLogMapper.updateOnVersion(FreezeLogStatusEnum.CONFIRM, freezeLog.getVersion(), freezeLog.getId());
        if (row != 1) {
            FreezeLog latest = freezeLogMapper.selectById(freezeLog.getId());
            if (latest != null && latest.getStatus().equals(FreezeLogStatusEnum.CONFIRM)) {
                //并发下已被确认，幂等返回
                return true;
            }
            throw new BusinessException("确认扣款失败，冻结记录版本冲突");
        }
        //扣减冻结金额：frozen_balance -= amount（乐观锁）
        Wallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("未找到用户");
        }
        long frozenBalance = wallet.getFrozenBalance() - freezeLog.getAmount();
        if (frozenBalance < 0) {
            throw new BusinessException("冻结金额异常");
        }
        LambdaUpdateWrapper<Wallet> wrapper = new LambdaUpdateWrapper<Wallet>()
                .eq(Wallet::getUserId, userId)
                .eq(Wallet::getVersion, wallet.getVersion())
                .set(Wallet::getFrozenBalance, frozenBalance)
                .set(Wallet::getVersion, wallet.getVersion() + 1);
        row = walletMapper.update(wrapper);
        if (row != 1) {
            throw new BusinessException("确认扣款失败，钱包版本冲突");
        }
        //记录流水
        walletLogMapper.insert(new WalletLog()
                .setUserId(userId)
                .setType(WalletLogTypeEnum.CONFIRM)
                .setAmount(freezeLog.getAmount())
                .setStatus(1));
        log.info("TCC确认扣款成功：bizId={} userId={} amount={}", bizId, userId, freezeLog.getAmount());
        return true;
    }

    // ==================== TCC Cancel：取消扣款 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackDeduct(BusinessActionContext actionContext) {
        Long userId = getLongFromContext(actionContext, "userId");
        Long amount = getLongFromContext(actionContext, "amount");
        String bizId = getStringFromContext(actionContext, "bizId");
        if (userId == null || amount == null || bizId == null) {
            throw new BusinessException("TCC上下文参数缺失，无法取消扣款");
        }
        FreezeLog freezeLog = getFreezeLog(bizId);
        if (freezeLog == null) {
            //空回滚：try未执行，插入CANCEL记录防悬挂
            freezeLogMapper.insert(new FreezeLog()
                    .setUserId(userId)
                    .setAmount(amount)
                    .setBizId(bizId)
                    .setStatus(FreezeLogStatusEnum.CANCEL));
            log.info("TCC空回滚：bizId={} 已记录CANCEL防悬挂", bizId);
            return true;
        }
        //幂等：已取消直接返回
        if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CANCEL)) {
            log.info("TCC取消：bizId={} 已取消，幂等返回", bizId);
            return true;
        }
        if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CONFIRM)) {
            //已确认扣款，不允许回滚
            throw new BusinessException("已确认扣款，无法取消");
        }
        //CAS：TRY -> CANCEL，保证只退款一次
        int row = freezeLogMapper.updateOnVersion(FreezeLogStatusEnum.CANCEL, freezeLog.getVersion(), freezeLog.getId());
        if (row != 1) {
            FreezeLog latest = freezeLogMapper.selectById(freezeLog.getId());
            if (latest != null) {
                if (latest.getStatus().equals(FreezeLogStatusEnum.CANCEL)) {
                    return true;
                }
                if (latest.getStatus().equals(FreezeLogStatusEnum.CONFIRM)) {
                    throw new BusinessException("已确认扣款，无法取消");
                }
            }
            throw new BusinessException("取消扣款失败，冻结记录版本冲突");
        }
        //解冻：frozen_balance -= amount，balance += amount（乐观锁）
        Wallet wallet = walletMapper.selectByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("未找到用户");
        }
        LambdaUpdateWrapper<Wallet> wrapper = new LambdaUpdateWrapper<Wallet>()
                .eq(Wallet::getUserId, userId)
                .eq(Wallet::getVersion, wallet.getVersion())
                .set(Wallet::getFrozenBalance, wallet.getFrozenBalance() - freezeLog.getAmount())
                .set(Wallet::getBalance, wallet.getBalance() + freezeLog.getAmount())
                .set(Wallet::getVersion, wallet.getVersion() + 1)
                .ge(Wallet::getFrozenBalance, freezeLog.getAmount());
        row = walletMapper.update(wrapper);
        if (row != 1) {
            throw new BusinessException("取消扣款失败，钱包版本冲突");
        }
        //记录流水
        walletLogMapper.insert(new WalletLog()
                .setUserId(userId)
                .setType(WalletLogTypeEnum.REFUND)
                .setAmount(freezeLog.getAmount())
                .setStatus(1));
        log.info("TCC取消扣款成功：bizId={} userId={} amount={}", bizId, userId, freezeLog.getAmount());
        return true;
    }

    // ==================== 工具方法 ====================

    /**
     * 按业务id查询冻结记录（bizId 无唯一索引，取最新一条保证幂等判断稳定）
     */
    private FreezeLog getFreezeLog(String bizId) {
        return freezeLogMapper.selectOne(new LambdaQueryWrapper<FreezeLog>()
                .eq(FreezeLog::getBizId, bizId)
                .orderByDesc(FreezeLog::getId)
                .last("LIMIT 1"));
    }

    /**
     * 从事务上下文读取 Long 值（上下文经过序列化，数值类型可能是 Integer/Long，统一 toString 转换）
     */
    private Long getLongFromContext(BusinessActionContext context, String key) {
        Map<String, Object> actionContext = context.getActionContext();
        Object value = actionContext.get(key);
        return value == null ? null : Long.valueOf(value.toString());
    }

    /**
     * 从事务上下文读取 String 值
     */
    private String getStringFromContext(BusinessActionContext context, String key) {
        Map<String, Object> actionContext = context.getActionContext();
        Object value = actionContext.get(key);
        return value == null ? null : value.toString();
    }
}