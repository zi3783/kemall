package com.kemall.account.service.impl.dubbo;

import com.kemall.account.service.IWalletService;
import com.kemall.api.dto.WalletDTO;
import com.kemall.api.dubbo.AccountDubboService;
import com.kemall.common.core.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.startup.UserConfig;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@DubboService
@Component
@RequiredArgsConstructor
public class AccountDubboServiceImpl implements AccountDubboService {

    private final IWalletService walletService;

    @Override
    public void deductWallet(WalletDTO walletDTO){
        walletService.deductByTcc(UserContext.getUserId(), walletDTO.getBalance(), walletDTO.getPaymentNo());
    }

}
