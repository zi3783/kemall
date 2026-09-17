package com.kemall.account.service.impl;

import com.kemall.account.service.IWalletService;
import com.kemall.api.dto.WalletDTO;
import com.kemall.api.dubbo.AccountDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@DubboService
@Component
@RequiredArgsConstructor
public class AccountDubboServiceImpl implements AccountDubboService {

    private final IWalletService walletService;

    @Override
    public void deductWallet(WalletDTO walletDTO){
        walletService.transaction(walletDTO);
    }

}
