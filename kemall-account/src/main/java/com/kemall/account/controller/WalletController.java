package com.kemall.account.controller;


import com.kemall.account.enums.WalletLogTypeEnum;
import com.kemall.account.service.IWalletService;
import com.kemall.api.dto.WalletDTO;
import com.kemall.common.annotation.LoginRequire;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2026-08-07
 */
@RestController
@RequestMapping("/wallets")
@Tag(name = "余额相关接口")
@RequiredArgsConstructor
@MapperScan("com.kemall.account.mapper")
public class WalletController {

    private final IWalletService walletService;

    @PostMapping("/recharge")
    @Operation(summary = "充值接口")
    public void charge(@RequestBody WalletDTO dto) {
        walletService.updateAccount(dto, WalletLogTypeEnum.RECHARGE);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "提现接口")
    public void withdraw(@RequestBody WalletDTO dto) {
        walletService.updateAccount(dto, WalletLogTypeEnum.WITHDRAW);
    }

    @PostMapping("/consume")
    @Operation(summary = "消费接口")
    public void consume(@RequestBody WalletDTO dto) {
        walletService.updateAccount(dto, WalletLogTypeEnum.CONSUME);
    }

    @PostMapping("/refund")
    @Operation(summary = "退款接口")
    public void refund(@RequestBody WalletDTO dto) {
        walletService.updateAccount(dto, WalletLogTypeEnum.REFUND);
    }

}
