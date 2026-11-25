package com.kemall.iam.auth.controller;

import com.kemall.iam.api.dto.auth.LoginRequest;
import com.kemall.iam.api.dto.auth.LoginResponse;
import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.auth.application.service.AuthAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iam/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证")
public class AuthController {

    private final AuthAppService authAppService;

    /**
     * 用户登录（密码）。白名单接口。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResult<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        return ApiResult.success(authAppService.login(request, servletRequest));
    }
}
