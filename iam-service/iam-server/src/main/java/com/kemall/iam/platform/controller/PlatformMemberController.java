package com.kemall.iam.platform.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.platform.CreatePlatformMemberRequest;
import com.kemall.iam.api.dto.platform.PlatformMemberInfo;
import com.kemall.iam.api.dto.platform.UpdatePlatformMemberStatusRequest;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.platform.application.service.PlatformMemberAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台管理员管理接口。
 * <p>
 * 平台角色分配沿用 permission 子域的 PUT /api/iam/v1/platform-members/{id}/roles。
 */
@RestController
@RequestMapping("/api/iam/v1/platform-members")
@RequiredArgsConstructor
@Tag(name = "平台管理员管理")
public class PlatformMemberController {

    private final PlatformMemberAppService platformMemberAppService;

    @Operation(summary = "平台成员分页查询")
    @GetMapping
    @PreAuthorize("hasAuthority('iam:platform:member:list')")
    public ApiResult<PageResult<PlatformMemberInfo>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(platformMemberAppService.page(
                SecurityPrincipalHolder.current(), keyword, status, page, size));
    }

    @Operation(summary = "平台成员详情（含已分配平台角色）")
    @GetMapping("/{platformMemberId}")
    @PreAuthorize("hasAuthority('iam:platform:member:read')")
    public ApiResult<PlatformMemberInfo> get(@PathVariable Long platformMemberId) {
        return ApiResult.success(
                platformMemberAppService.get(SecurityPrincipalHolder.current(), platformMemberId));
    }

    @Operation(summary = "创建平台成员（已有账号提拔）")
    @PostMapping
    @PreAuthorize("hasAuthority('iam:platform:member:create')")
    public ApiResult<PlatformMemberInfo> create(@Valid @RequestBody CreatePlatformMemberRequest request) {
        return ApiResult.success(
                platformMemberAppService.create(SecurityPrincipalHolder.current(), request));
    }

    @Operation(summary = "平台成员禁用/启用")
    @PatchMapping("/{platformMemberId}/status")
    @PreAuthorize("hasAuthority('iam:platform:member:status')")
    public ApiResult<Void> changeStatus(@PathVariable Long platformMemberId,
                                        @Valid @RequestBody UpdatePlatformMemberStatusRequest request) {
        platformMemberAppService.changeStatus(
                SecurityPrincipalHolder.current(), platformMemberId, request.getStatus());
        return ApiResult.success();
    }
}
