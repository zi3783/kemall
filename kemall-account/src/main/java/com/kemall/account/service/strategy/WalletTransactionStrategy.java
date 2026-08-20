package com.kemall.account.service.strategy;

import com.kemall.account.domain.po.Wallet;
import com.kemall.api.dto.WalletDTO;

public interface WalletTransactionStrategy {
    Wallet execute(WalletDTO dto);
}
