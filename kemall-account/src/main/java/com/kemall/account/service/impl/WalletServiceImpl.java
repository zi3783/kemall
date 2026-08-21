package com.kemall.account.service.impl;

import com.kemall.account.annotation.RedissonLock;
import com.kemall.account.constants.RedisConstant;
import com.kemall.account.domain.po.FreezeLog;
import com.kemall.account.domain.po.Wallet;
import com.kemall.account.domain.po.WalletLog;
import com.kemall.account.enums.AccountStatusEnum;
import com.kemall.account.enums.FreezeLogStatusEnum;
import com.kemall.account.enums.WalletLogTypeEnum;
import com.kemall.account.mapper.FreezeLogMapper;
import com.kemall.account.mapper.WalletLogMapper;
import com.kemall.account.mapper.WalletMapper;
import com.kemall.account.service.IWalletService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.account.service.strategy.WalletTransactionFactory;
import com.kemall.account.service.strategy.WalletTransactionStrategy;
import com.kemall.api.dto.WalletDTO;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceImpl extends ServiceImpl<WalletMapper, Wallet> implements IWalletService {

    private final WalletMapper walletMapper;

    private final WalletLogMapper walletLogMapper;

    private final WalletTransactionFactory walletTransactionFactory;

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Boolean> redisScript;

    private final FreezeLogMapper freezeLogMapper;

    @Override
    public void transaction(WalletDTO walletDTO) {
        //选择业务执行
        WalletTransactionStrategy execute = walletTransactionFactory.getTransactionStrategy(walletDTO.getTransactionType());
        Wallet account = execute.execute(walletDTO);
        //更新缓存
        Boolean isSuccess = redisTemplate.execute(
                redisScript,
                Collections.singletonList(RedisConstant.ACCOUNT_PREFIX + walletDTO.getUserId()),
                account.getVersion().toString(),
                account.getBalance().toString()
        );
        if(isSuccess){
            log.info("缓存已经刷新");
        }else{
            log.info("版本可能不是最新的");
        }
    }

    @Override
    @Transactional
    public Wallet changeAmount(Long userId, Long balance) {
        //先查账户
        int i = 0;
        Wallet account = null;
        for(; i < 3; ++i){
            account = lambdaQuery()
                    .eq(Wallet::getUserId, userId)
                    .one();
            if (account == null) {
                //用户不存在
                //创建用户
                Wallet wallet = new Wallet();
                wallet.setUserId(userId)
                        .setStatus(AccountStatusEnum.NORMAL)
                        .setBalance(balance)
                        .setVersion(0);
                save(wallet);
                account = wallet;
            }
            //检查账户
            if (account.getStatus() == AccountStatusEnum.FROZEN) {
                throw new BusinessException("目标账户冻结");
            }
            Long amount = account.getBalance() + balance;
            if (amount < 0) {
                throw new BusinessException("余额不足");
            }
            //修改数据
            Integer row = walletMapper.updateBalance(userId, amount, account.getVersion());
            if (row == 1) {
                account.setVersion(account.getVersion() + 1);
                account.setBalance(balance + account.getBalance());
                break;
            }
        }
        //保存记录
        WalletLog walletLog = new WalletLog();
        walletLog.setAmount(balance > 0 ? balance : -balance);
        walletLog.setType(balance > 0 ? WalletLogTypeEnum.RECHARGE : WalletLogTypeEnum.DEDUCT);
        walletLog.setUserId(userId);
        walletLog.setStatus(i == 0 ? 1 : 2);
        walletLogMapper.insert(walletLog);
        if(i == 3){
            throw new BusinessException("系统繁忙");
        }
        return account;
    }

    @Override
    public Long queryBalanceByUserId() {
        //获得用户id
        Long userId = UserContext.getUserId();
        //查询redis缓存
        String json = (String) redisTemplate.opsForHash()
                .get(RedisConstant.ACCOUNT_PREFIX + userId, "balance");
        if(json != null){
            log.debug("从缓存中读取数据");
            return Long.valueOf(json);
        }
        //查询mysql
        Wallet one = lambdaQuery()
                .select(Wallet::getBalance, Wallet::getVersion)
                .eq(Wallet::getUserId, userId)
                .one();
        if(one == null) {
            throw new BusinessException("用户不存在");
        }
        //更新redis 大部分情况不会查询到mysql,只有当缓存没有才会到这里
        redisTemplate.opsForHash().put(RedisConstant.ACCOUNT_PREFIX + userId, "balance", one.getBalance().toString());
        redisTemplate.opsForHash().put(RedisConstant.ACCOUNT_PREFIX + userId, "version", one.getVersion().toString());
        //结束
        log.debug("从mysql中读取到数据data = {}", one.getBalance());
        return one.getBalance();
    }

    @Override
    public boolean freezeAmount(Long balance, String bizId, Long userId) {
        if(balance <= 0 || bizId == null || userId == null){
            throw new IllegalArgumentException("参数错误");
        }
        IWalletService walletService = (IWalletService) AopContext.currentProxy();
        Wallet one = walletService.getWalletAndUpdate(balance, userId, bizId);
        if (one == null) return false;
        //更新缓存
        Long wallet = one.getBalance();
        Integer version = one.getVersion();
        redisTemplate.opsForHash().put(RedisConstant.ACCOUNT_PREFIX + userId, "balance", wallet.toString());
        redisTemplate.opsForHash().put(RedisConstant.ACCOUNT_PREFIX + userId, "version", version.toString());
        return true;
    }

    @RedissonLock(key = "#userId", waitTime = 3, prefix = "Account:UserId:Lock")
    @Transactional
    public @Nullable Wallet getWalletAndUpdate(Long balance, Long userId, String bizId) {
        //防悬挂 取消请求先到达 创建请求后到达
        //先查freeze_log表，如果已经cancel那么无需再插入
        HashMap<String, Object> map = new HashMap<>();
        map.put("biz_id", bizId);
        List<FreezeLog> list = freezeLogMapper.selectByMap(map);
        if(!list.isEmpty()){
            if(list.size() != 1){
                throw new BusinessException("数据异常！");
            }
            FreezeLog freezeLog = list.get(0);
            if(freezeLog.getStatus().equals(FreezeLogStatusEnum.CANCEL)){
                log.info("已取消，不需要冻结");
            } else if (freezeLog.getStatus().equals(FreezeLogStatusEnum.TRY)) {
                log.info("已经冻结，请勿重复操作");
            } else if (freezeLog.getStatus().equals(FreezeLogStatusEnum.CONFIRM)) {
                log.info("此订单已经支付");
            }
            return null;
        }
        //查出旧数据
        Wallet one = lambdaQuery()
                .eq(Wallet::getUserId, userId)
                .one();
        if(one == null) {
            log.debug("没有找到账户");
            return null;
        }
        //冻结金额
        if(one.getStatus() == AccountStatusEnum.FROZEN){
            log.info("账户被冻结");
            return null;
        }
        if(one.getBalance() < balance){
            log.info("账户余额不足");
            return null;
        }
        Integer row = walletMapper.freezeBalance(userId, balance, one.getVersion());
        if(row != 1){
            throw new BusinessException("冻结失败，版本号错误");
        }
        //记录日志
        FreezeLog freezeLog = new FreezeLog();
        freezeLog.setAmount(balance);
        freezeLog.setUserId(userId);
        freezeLog.setBizId(bizId);
        freezeLog.setStatus(FreezeLogStatusEnum.TRY);
        freezeLogMapper.insert(freezeLog);

        one.setBalance(one.getBalance() - balance);
        one.setVersion(one.getVersion() + 1);
        return one;
    }


}
