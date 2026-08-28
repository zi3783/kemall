package com.kemall.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kemall.common.annotation.RedissonLock;
import com.kemall.account.domain.po.FreezeLog;
import com.kemall.account.domain.po.Wallet;
import com.kemall.account.domain.po.WalletLog;
import com.kemall.account.enums.FreezeLogStatusEnum;
import com.kemall.account.enums.WalletLogTypeEnum;
import com.kemall.account.mapper.FreezeLogMapper;
import com.kemall.account.mapper.WalletLogMapper;
import com.kemall.account.mapper.WalletMapper;
import com.kemall.account.service.IFreezeLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 冻结日志表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FreezeLogServiceImpl extends ServiceImpl<FreezeLogMapper, FreezeLog> implements IFreezeLogService {

    private final FreezeLogMapper freezeLogMapper;

    private final WalletLogMapper walletLogMapper;

    private final WalletMapper walletMapper;

    @Override
    public boolean confirmAccount(Long freezeLogId) {
        if(freezeLogId == null){
            throw new IllegalArgumentException("freezeLogId is null");
        }

        FreezeLogServiceImpl freezeLogService = (FreezeLogServiceImpl) AopContext.currentProxy();
        return freezeLogService.confirmLogic(freezeLogId, UserContext.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#userId", prefix = "Account:UserId:Lock:", waitTime = 3)
    @Override
    public boolean confirmLogic(Long freezeLogId, Long userId) {
        try {
            for (int i = 0; i < 3; ++i) {
                FreezeLog freezeLog = lambdaQuery().eq(FreezeLog::getId, freezeLogId).one();
                if (freezeLog == null) {
                    throw new BusinessException("未找到冻结记录");
                }
                //检查是不是当前用户
                if (!freezeLog.getUserId().equals(userId)) {
                    throw new BusinessException("用户错误");
                }
                //检查日志状态
                if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CONFIRM)) {
                    FreezeLogServiceImpl.log.info("已经扣款");
                    return true;
                }
                if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CANCEL)) {
                    FreezeLogServiceImpl.log.info("已经取消");
                    return false;
                }
                //修改日志
                int row = freezeLogMapper.updateOnVersion(FreezeLogStatusEnum.CONFIRM, freezeLog.getVersion(), freezeLogId);
                if (row != 1) {
                    log.info("freezeLog数据有变动，重新执行");
                    Thread.sleep(1000);
                    continue;
                }
                //更新wallet表信息
                //1.查找wallet中的version字段
                Wallet wallet = walletMapper.selectByUserId(userId);
                if (wallet == null) {
                    throw new BusinessException("未找到用户");
                }
                Integer version = wallet.getVersion();
                //2.更新
                Long frozenBalance = wallet.getFrozenBalance() - freezeLog.getAmount();
                LambdaUpdateWrapper<Wallet> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(Wallet::getUserId, userId)
                        .eq(Wallet::getVersion, version)
                        .set(Wallet::getFrozenBalance, frozenBalance)
                        .set(Wallet::getVersion, version + 1);
                row = walletMapper.update(wrapper);
                if (row != 1) {
                    throw new RuntimeException("系统繁忙");
                }
                //记录日志
                WalletLog walletLog = new WalletLog()
                        .setUserId(freezeLog.getUserId())
                        .setType(WalletLogTypeEnum.CONFIRM)
                        .setAmount(freezeLog.getAmount())
                        .setStatus(1);
                walletLogMapper.insert(walletLog);
                return true;
            }
            throw new BusinessException("系统繁忙，请稍后重试");
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean cancelAccount(Long freezeLogId) {
        if(freezeLogId == null){
            throw new IllegalArgumentException("freezeLogId is null");
        }
        FreezeLogServiceImpl freezeLogService = (FreezeLogServiceImpl) AopContext.currentProxy();
        return freezeLogService.cancelLogic(freezeLogId, UserContext.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#userId", waitTime = 3, prefix = "Account:UserId:Lock:")
    public boolean cancelLogic(Long freezeLogId, Long userId) {
        try {
            for (int i = 0; i < 3; ++i) {
                //查询freeze_log
                FreezeLog user = lambdaQuery().eq(FreezeLog::getId, freezeLogId).one();
                if(user == null){
                    throw new BusinessException("记录不存在");
                }
                //校验用户
                if(!userId.equals(user.getUserId())){
                    throw new BusinessException("用户信息错误");
                }
                //检查日志状态
                if(user.getStatus().equals(FreezeLogStatusEnum.CONFIRM)){
                    log.info("已经付款");
                    return false;
                }
                if(user.getStatus().equals(FreezeLogStatusEnum.CANCEL)){
                    log.info("已经取消");
                    return true;
                }
                //修改日志
                int row = freezeLogMapper.updateOnVersion(FreezeLogStatusEnum.CANCEL, user.getVersion(), freezeLogId);
                if(row != 1){
                    log.info("freezeLog数据更改");
                    Thread.sleep(1000);
                    continue;
                }
                //执行退款逻辑
                //1.查询wallet表
                Wallet wallet = walletMapper.selectByUserId(user.getUserId());
                if(wallet == null){
                    throw new BusinessException("未找到用户");
                }
                //2.退款
                LambdaUpdateWrapper<Wallet> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(Wallet::getUserId, userId)
                        .eq(Wallet::getVersion, wallet.getVersion())
                        .set(Wallet::getVersion, wallet.getVersion() + 1)
                        .set(Wallet::getFrozenBalance, wallet.getFrozenBalance() - user.getAmount())
                        .set(Wallet::getBalance, wallet.getBalance() + user.getAmount())
                        .ge(Wallet::getFrozenBalance, user.getAmount());
                row = walletMapper.update(wrapper);
                if(row != 1){
                    throw new RuntimeException("系统繁忙");
                }
                //记录wallet_log日志
                WalletLog walletLog = new WalletLog()
                        .setUserId(userId)
                        .setType(WalletLogTypeEnum.REFUND)
                        .setAmount(user.getAmount())
                        .setStatus(1);
                walletLogMapper.insert(walletLog);
            }
            throw new BusinessException("系统繁忙");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
