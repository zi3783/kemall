package com.kemall.account.controller;


import com.kemall.account.service.IWalletService;
import com.kemall.api.dto.WalletDTO;
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

    @PostMapping("transactions")
    @Operation(summary = "交易相关接口")
    public void transactions(@RequestBody WalletDTO walletDTO) {
        walletService.transaction(walletDTO);
    }
}
