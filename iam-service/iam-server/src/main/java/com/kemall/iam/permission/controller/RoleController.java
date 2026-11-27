package com.kemall.iam.permission.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.permission.RoleDetailInfo;
import com.kemall.iam.api.dto.permission.RoleInfo;
import com.kemall.iam.api.dto.permission.RolePermissionPatchRequest;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.permission.application.service.PermissionAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色中心（需登录 + 权限）。
 */
@RestController
@RequestMapping("/api/iam/v1/roles")
@RequiredArgsConstructor
@Tag(name = "角色")
public class RoleController {

    private final PermissionAppService permissionAppService;

    @Operation(summary = "当前工作空间下的角色列表")
    @GetMapping
    @PreAuthorize("hasAuthority('iam:role:list')")
    public ApiResult<List<RoleInfo>> listRoles() {
        return ApiResult.success(permissionAppService.listRoles(SecurityPrincipalHolder.current()));
    }

    @Operation(summary = "角色详情（含已关联权限）")
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('iam:role:read')")
    public ApiResult<RoleDetailInfo> getRole(@PathVariable Long roleId) {
        return ApiResult.success(permissionAppService.getRole(SecurityPrincipalHolder.current(), roleId));
    }

    @Operation(summary = "角色-权限增量关联")
    @PatchMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('iam:role:permission:update')")
    public ApiResult<Void> patchRolePermissions(@PathVariable Long roleId,
                                                @Valid @RequestBody RolePermissionPatchRequest request) {
        permissionAppService.patchRolePermissions(SecurityPrincipalHolder.current(), roleId, request);
        return ApiResult.success();
    }
}
