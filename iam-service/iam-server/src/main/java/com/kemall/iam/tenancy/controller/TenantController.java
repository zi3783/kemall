package com.kemall.iam.tenancy.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.tenancy.CreateTenantRequest;
import com.kemall.iam.api.dto.tenancy.TenantInfo;
import com.kemall.iam.api.dto.tenancy.UpdateTenantStatusRequest;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.tenancy.application.service.TenantAppService;
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
 * 租户管理接口。
 */
@RestController
@RequestMapping("/api/iam/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "租户管理")
public class TenantController {

    private final TenantAppService tenantAppService;

    @Operation(summary = "租户分页查询")
    @GetMapping
    @PreAuthorize("hasAuthority('iam:tenant:list')")
    public ApiResult<PageResult<TenantInfo>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(tenantAppService.page(
                SecurityPrincipalHolder.current(), keyword, status, page, size));
    }

    @Operation(summary = "租户详情")
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasAuthority('iam:tenant:read')")
    public ApiResult<TenantInfo> get(@PathVariable Long tenantId) {
        return ApiResult.success(tenantAppService.get(SecurityPrincipalHolder.current(), tenantId));
    }

    @Operation(summary = "创建租户")
    @PostMapping
    @PreAuthorize("hasAuthority('iam:tenant:create')")
    public ApiResult<TenantInfo> create(@Valid @RequestBody CreateTenantRequest request) {
        return ApiResult.success(tenantAppService.create(SecurityPrincipalHolder.current(), request));
    }

    @Operation(summary = "租户状态变更")
    @PatchMapping("/{tenantId}/status")
    @PreAuthorize("hasAuthority('iam:tenant:status')")
    public ApiResult<Void> changeStatus(@PathVariable Long tenantId,
                                        @Valid @RequestBody UpdateTenantStatusRequest request) {
        tenantAppService.changeStatus(
                SecurityPrincipalHolder.current(), tenantId, request.getStatus());
        return ApiResult.success();
    }
}
