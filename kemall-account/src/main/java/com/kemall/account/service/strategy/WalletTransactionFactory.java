package com.kemall.account.service.strategy;

import com.kemall.account.service.strategy.impl.WalletDeductStrategy;
import com.kemall.account.service.strategy.impl.WalletRechargeTransaction;
import com.kemall.api.enums.TransactionType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
public class WalletTransactionFactory {

    private final EnumMap<TransactionType, WalletTransactionStrategy> transactionStrategyMap;

    public WalletTransactionFactory(@Lazy WalletRechargeTransaction recharge,
                                    @Lazy WalletDeductStrategy deduct) {
        transactionStrategyMap = new EnumMap<>(TransactionType.class);
        transactionStrategyMap.put(TransactionType.RECHARGE, recharge);
        transactionStrategyMap.put(TransactionType.CONSUME, deduct);
        //todo 添加策略
    }

    public WalletTransactionStrategy getTransactionStrategy(TransactionType transactionType) {
        if(transactionType == null) {
            throw new NullPointerException("transactionType is null");
        }
        return transactionStrategyMap.get(transactionType);
    }

}
