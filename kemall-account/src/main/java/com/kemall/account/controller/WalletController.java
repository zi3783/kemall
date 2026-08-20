package com.kemall.account.controller;


import com.kemall.account.service.IWalletService;
import com.kemall.api.dto.WalletDTO;
import com.kemall.common.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/wallets")
@Tag(name = "余额相关接口")
@RequiredArgsConstructor
@MapperScan("com.kemall.account.mapper")
public class WalletController {

    private final IWalletService walletService;

    @PostMapping("transactions")
    @Operation(summary = "交易相关接口")
    public Result transactions(@RequestBody WalletDTO walletDTO) {
        walletService.transaction(walletDTO);
        return Result.success();
    }


    @GetMapping("balance")
    @Operation(summary = "查询余额")
    public Result<Long> getBalance() {
        Long balance = walletService.queryBalanceByUserId();
        return Result.success(balance);
    }

}
