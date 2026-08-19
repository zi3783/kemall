package com.kemall.account.service.strategy;

import com.kemall.api.dto.WalletDTO;

public interface WalletTransactionStrategy {
    public void execute(WalletDTO dto);
}
