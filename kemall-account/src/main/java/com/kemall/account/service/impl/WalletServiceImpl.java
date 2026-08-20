package com.kemall.account.service.impl;

import com.kemall.account.constants.RedisConstant;
import com.kemall.account.domain.po.Wallet;
import com.kemall.account.domain.po.WalletLog;
import com.kemall.account.enums.AccountStatusEnum;
import com.kemall.account.enums.WalletLogTypeEnum;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

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
        if(i > 0){
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
}
