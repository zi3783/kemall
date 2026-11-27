package com.kemall.iam.permission.controller;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.api.dto.permission.MemberRoleAssignRequest;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.permission.application.service.RoleAssignmentAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成员角色分配（需登录 + 权限）。
 */
@RestController
@RequestMapping("/api/iam/v1")
@RequiredArgsConstructor
@Tag(name = "角色分配")
public class RoleAssignmentController {

    private final RoleAssignmentAppService roleAssignmentAppService;

    @Operation(summary = "租户成员角色分配（全量覆盖）")
    @PutMapping("/tenant-members/{memberId}/roles")
    @PreAuthorize("hasAuthority('iam:member:role:assign')")
    public ApiResult<Void> assignTenantMemberRoles(@PathVariable Long memberId,
                                                   @Valid @RequestBody MemberRoleAssignRequest request) {
        roleAssignmentAppService.assignTenantMemberRoles(
                SecurityPrincipalHolder.current(), memberId, request);
        return ApiResult.success();
    }

    @Operation(summary = "平台成员角色分配（全量覆盖）")
    @PutMapping("/platform-members/{platformMemberId}/roles")
    @PreAuthorize("hasAuthority('iam:platform:role:assign')")
    public ApiResult<Void> assignPlatformMemberRoles(@PathVariable Long platformMemberId,
                                                      @Valid @RequestBody MemberRoleAssignRequest request) {
        roleAssignmentAppService.assignPlatformMemberRoles(
                SecurityPrincipalHolder.current(), platformMemberId, request);
        return ApiResult.success();
    }
}
