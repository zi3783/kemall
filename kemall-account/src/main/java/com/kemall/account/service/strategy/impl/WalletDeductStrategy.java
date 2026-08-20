package com.kemall.account.service.strategy.impl;

import com.kemall.account.annotation.RedissonLock;
import com.kemall.account.domain.po.Wallet;
import com.kemall.account.service.IWalletService;
import com.kemall.account.service.strategy.WalletTransactionStrategy;
import com.kemall.api.dto.WalletDTO;
import com.kemall.api.enums.TransactionType;
import com.kemall.common.exception.BusinessException;
import com.kemall.common.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalletDeductStrategy implements WalletTransactionStrategy {


    @Autowired
    private IWalletService walletService;

    @Override
    @RedissonLock(key = "#dto.userId", waitTime = 3, prefix = "Account:UserId:Lock:")
    public Wallet execute(WalletDTO dto) {
        //检查参数
        if(dto == null || dto.getBalance() == null || dto.getBalance() <= 0 || dto.getUserId() == null || dto.getTransactionType() == null){
            throw new IllegalArgumentException("扣款参数错误 walletDto");
        }
        //判断了业务类型
        if(dto.getTransactionType() != TransactionType.RECHARGE){
            throw new BusinessException("扣款业务类型错误");
        }
        //核对用户
        Long userId = UserContext.getUserId();
        if(!userId.equals(dto.getUserId())){
            throw new BusinessException("扣款用户信息错误");
        }

        return walletService.changeAmount(userId, - dto.getBalance());
        //结束
    }
}
