package com.kemall.account.service.impl;

import com.kemall.account.domain.po.Wallet;
import com.kemall.account.domain.po.WalletLog;
import com.kemall.account.enums.AccountStatusEnum;
import com.kemall.account.enums.WalletLogTypeEnum;
import com.kemall.account.mapper.WalletLogMapper;
import com.kemall.account.mapper.WalletMapper;
import com.kemall.account.service.IWalletService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kemall.account.service.strategy.WalletTransactionContext;
import com.kemall.account.service.strategy.WalletTransactionStrategy;
import com.kemall.api.dto.WalletDTO;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
@Service
@RequiredArgsConstructor
public class WalletServiceImpl extends ServiceImpl<WalletMapper, Wallet> implements IWalletService {

    private final WalletMapper walletMapper;

    private final WalletLogMapper walletLogMapper;

    private final WalletTransactionContext walletTransactionContext;

    @Override
    public void transaction(WalletDTO walletDTO) {
        WalletTransactionStrategy execute = walletTransactionContext.getTransactionStrategy(walletDTO.getTransactionType());
        execute.execute(walletDTO);
    }

    @Override
    @Transactional
    public void changeAmount(Long userId, Long balance) {
        //先查账户
        int i = 0;
        for(; i < 3; ++i){
            Wallet account = lambdaQuery()
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
    }


}
