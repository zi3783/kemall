package com.kemall.api.dubbo;

import com.kemall.api.domain.dto.WalletDTO;

public interface AccountDubboService {
    void deductWallet(WalletDTO walletDTO);
}
