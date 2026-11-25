package com.kemall.iam.auth.controller;

import com.kemall.iam.api.dto.auth.ChangePasswordRequest;
import com.kemall.iam.api.dto.auth.LogoutRequest;
import com.kemall.iam.api.dto.auth.RefreshTokenRequest;
import com.kemall.iam.api.dto.auth.SessionInfo;
import com.kemall.iam.api.dto.auth.TokenInfo;
import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.auth.application.service.SessionAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话/密码/刷新/登出 控制器。
 * <p>
 * 除 {@code /refresh} 外全部受 JWT + Redis Session 保护。
 */
@RestController
@RequestMapping("/api/iam/v1/auth")
@RequiredArgsConstructor
@Tag(name = "会话与安全")
public class SessionController {

    private final SessionAppService sessionAppService;

    @Operation(summary = "刷新 Token（白名单）")
    @PostMapping("/refresh")
    public ApiResult<TokenInfo> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResult.success(sessionAppService.refresh(request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        sessionAppService.logout(request == null ? new LogoutRequest() : request);
        return ApiResult.success();
    }

    @Operation(summary = "修改密码（保留当前会话，撤销其他所有设备）")
    @PutMapping("/password")
    public ApiResult<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        sessionAppService.changePassword(request);
        return ApiResult.success();
    }

    @Operation(summary = "当前用户的登录设备/会话列表")
    @GetMapping("/sessions")
    public ApiResult<List<SessionInfo>> listSessions() {
        return ApiResult.success(sessionAppService.listSessions());
    }

    @Operation(summary = "踢出指定设备/会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResult<Void> kickSession(@PathVariable String sessionId) {
        sessionAppService.kickSession(sessionId);
        return ApiResult.success();
    }
}
