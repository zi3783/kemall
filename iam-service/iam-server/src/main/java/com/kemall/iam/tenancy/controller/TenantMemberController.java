package com.kemall.iam.tenancy.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.common.PageResult;
import com.kemall.iam.api.dto.tenancy.AddTenantMemberRequest;
import com.kemall.iam.api.dto.tenancy.TenantMemberInfo;
import com.kemall.iam.api.dto.tenancy.UpdateMemberStatusRequest;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.tenancy.application.service.TenantMemberAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户/店铺成员管理接口。
 */
@RestController
@RequestMapping("/api/iam/v1/tenant-members")
@RequiredArgsConstructor
@Tag(name = "成员管理")
public class TenantMemberController {

    private final TenantMemberAppService tenantMemberAppService;

    @Operation(summary = "成员分页查询")
    @GetMapping
    @PreAuthorize("hasAuthority('iam:member:list')")
    public ApiResult<PageResult<TenantMemberInfo>> page(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer memberType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.success(tenantMemberAppService.page(
                SecurityPrincipalHolder.current(), tenantId, shopId, memberType, status, page, size));
    }

    @Operation(summary = "成员加入")
    @PostMapping
    @PreAuthorize("hasAuthority('iam:member:add')")
    public ApiResult<Long> add(@Valid @RequestBody AddTenantMemberRequest request) {
        return ApiResult.success(
                tenantMemberAppService.add(SecurityPrincipalHolder.current(), request));
    }

    @Operation(summary = "成员禁用/启用")
    @PatchMapping("/{memberId}/status")
    @PreAuthorize("hasAuthority('iam:member:status')")
    public ApiResult<Void> changeStatus(@PathVariable Long memberId,
                                        @RequestParam(required = false) Long tenantId,
                                        @Valid @RequestBody UpdateMemberStatusRequest request) {
        tenantMemberAppService.changeStatus(
                SecurityPrincipalHolder.current(), memberId, tenantId, request.getStatus());
        return ApiResult.success();
    }

    @Operation(summary = "成员移除")
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAuthority('iam:member:remove')")
    public ApiResult<Void> remove(@PathVariable Long memberId,
                                  @RequestParam(required = false) Long tenantId) {
        tenantMemberAppService.remove(SecurityPrincipalHolder.current(), memberId, tenantId);
        return ApiResult.success();
    }
}
