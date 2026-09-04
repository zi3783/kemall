package com.kemall.account.controller;

import com.kemall.account.service.IFreezeLogService;
import com.kemall.account.service.IWalletService;
import com.kemall.api.dto.WalletDTO;
import com.kemall.common.annotation.LoginRequire;
import com.kemall.common.utils.bean.result.Result;
import com.kemall.common.utils.UserContext;
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

    private final IFreezeLogService freezeLogService;

//    @PostMapping("/transactions")
//    @Operation(summary = "交易相关接口")
//    public Result<String> transactions(@RequestBody WalletDTO walletDTO) {
//        walletService.transaction(walletDTO);
//        return Result.success();
//    }

    @GetMapping("/balance")
    @Operation(summary = "查询余额") //todo 有问题
    public Result<Long> getBalance() {
        Long balance = walletService.queryBalanceByUserId();
        return Result.success(balance);
    }

//    @PutMapping("/freeze")
//    @Operation(summary = "冻结账户")
//    public Result<String> freezeAmount(Long balance, String bizId) {
//        return walletService.freezeAmount(balance, bizId, UserContext.getUserId()) ? Result.success() : Result.fail("冻结未成功，请查看余额或已取消");
//    }
//
//    @PutMapping("/confirm")
//    @Operation(summary = "确认扣款")
//    public Result<String> confirmAccount(Long freezeLogId) {
//        return freezeLogService.confirmAccount(freezeLogId) ? Result.success() : Result.fail("扣款失败");
//    }
//
//    @PutMapping("/cancel")
//    @Operation(summary = "取消扣款")
//    public Result<String> cancelAccount(Long freezeLogId){
//        return freezeLogService.cancelAccount(freezeLogId) ? Result.success() : Result.fail("取消失败");
//    }

    @PostMapping("/tcc/deduct")
    @LoginRequire(login = false)
    @Operation(summary = "TCC事务扣款（Try冻结，全局提交后确认扣款）")
    public Result<String> deductByTcc(Long userId, Long amount, String bizId) {
        return walletService.deductByTcc(userId, amount, bizId) ? Result.success() : Result.fail("扣款失败");
    }


}
