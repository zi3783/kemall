package com.kemall.account.controller;

import com.kemall.account.domain.query.WalletLogPage;
import com.kemall.account.domain.result.PageResult;
import com.kemall.account.domain.vo.WalletLogVO;
import com.kemall.account.service.IWalletLogService;
import com.kemall.common.utils.bean.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/wallet-log")
@Tag(name = "流水相关接口")
@RequiredArgsConstructor
public class WalletLogController {

    private final IWalletLogService walletLogService;

    @PostMapping("page")
    @Operation(summary = "分页查询流水")
    public Result<PageResult<WalletLogVO>> queryWalletLogByPage(WalletLogPage page){
        return Result.success(walletLogService.queryWalletLogByPage(page));
    }
}
