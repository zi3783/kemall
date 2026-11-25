package com.kemall.iam.user.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.user.RegisterRequest;
import com.kemall.iam.api.dto.user.RegisterResponse;
import com.kemall.iam.api.dto.user.ResetPasswordRequest;
import com.kemall.iam.api.dto.user.SendVerificationCodeRequest;
import com.kemall.iam.api.dto.user.VerificationCodeResponse;
import com.kemall.iam.user.application.service.AccountAppService;
import com.kemall.iam.user.application.service.VerificationCodeAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号控制器：注册、密码重置、验证码发送（白名单接口）。
 */
@RestController
@RequestMapping("/api/iam/v1/user")
@RequiredArgsConstructor
@Tag(name = "账号")
public class AccountController {

    private final AccountAppService accountAppService;
    private final VerificationCodeAppService verificationCodeAppService;

    @Operation(summary = "账号注册")
    @PostMapping("/register")
    public ApiResult<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResult.success(accountAppService.register(request));
    }

    @Operation(summary = "密码重置（手机号/邮箱 + 验证码）")
    @PostMapping("/password/reset")
    public ApiResult<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        accountAppService.resetPassword(request);
        return ApiResult.success();
    }

    @Operation(summary = "发送验证码")
    @PostMapping("/verification-code")
    public ApiResult<VerificationCodeResponse> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        return ApiResult.success(verificationCodeAppService.send(request));
    }
}
