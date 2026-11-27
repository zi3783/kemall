package com.kemall.iam.permission.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.permission.CurrentPermissionInfo;
import com.kemall.iam.api.dto.permission.PermissionInfo;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.permission.application.service.PermissionAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限中心查询接口。
 */
@RestController
@RequestMapping("/api/iam/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "权限")
public class PermissionController {

    private final PermissionAppService permissionAppService;

    @Operation(summary = "权限字典")
    @GetMapping
    @PreAuthorize("hasAuthority('iam:permission:list')")
    public ApiResult<List<PermissionInfo>> listPermissions() {
        return ApiResult.success(permissionAppService.listPermissions());
    }

    @Operation(summary = "当前用户权限")
    @GetMapping("/me")
    public ApiResult<CurrentPermissionInfo> currentPermissions() {
        return ApiResult.success(permissionAppService.currentPermissions(SecurityPrincipalHolder.current()));
    }

    @Operation(summary = "刷新当前会话权限缓存")
    @PostMapping("/refresh")
    public ApiResult<CurrentPermissionInfo> refreshPermissions() {
        return ApiResult.success(
                permissionAppService.refreshCurrentPermissions(SecurityPrincipalHolder.current()));
    }
}
