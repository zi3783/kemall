package com.kemall.iam.permission.application.service;

import com.kemall.iam.api.dto.permission.MemberRoleAssignRequest;
import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.permission.application.port.PermissionCache;
import com.kemall.iam.permission.application.support.PermissionScopeFactory;
import com.kemall.iam.permission.domain.exception.PermissionAccessException;
import com.kemall.iam.permission.domain.exception.PermissionErrorCode;
import com.kemall.iam.permission.domain.model.PermissionScope;
import com.kemall.iam.permission.domain.model.Role;
import com.kemall.iam.permission.domain.repository.RoleAssignmentRepository;
import com.kemall.iam.permission.domain.service.RoleAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * 成员角色分配应用服务。
 * <p>
 * 租户成员只允许在当前【租户级】工作空间分配；
 * 平台成员只允许在当前【平台】工作空间分配。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAssignmentAppService {

    private final RoleAccessService roleAccessService;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final PermissionCache permissionCache;

    /**
     * 租户成员角色分配（全量覆盖）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignTenantMemberRoles(LoginPrincipal principal, Long memberId,
                                        MemberRoleAssignRequest request) {
        PermissionScope scope = PermissionScopeFactory.from(principal);
        if (WorkspaceType.TENANT != scope.workspaceType()) {
            throw new PermissionAccessException(PermissionErrorCode.ROLE_ACCESS_DENIED);
        }
        if (!roleAssignmentRepository.tenantMemberExists(scope.tenantId(), memberId)) {
            throw new PermissionAccessException(PermissionErrorCode.TENANT_MEMBER_UNAVAILABLE);
        }

        List<Role> roles = roleAccessService.requireAssignableRoles(request.getRoleIds(), scope);
        List<Long> roleIds = new LinkedHashSet<>(roles.stream().map(Role::getId).toList())
                .stream().toList();

        roleAssignmentRepository.replaceMemberRoles(scope.tenantId(), memberId, roleIds);
        permissionCache.invalidateAll();
        log.info("[PERMISSION] 租户成员角色分配 tenantId={} memberId={} roleIds={}",
                scope.tenantId(), memberId, roleIds);
    }

    /**
     * 平台成员角色分配（全量覆盖）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPlatformMemberRoles(LoginPrincipal principal, Long platformMemberId,
                                          MemberRoleAssignRequest request) {
        PermissionScope scope = PermissionScopeFactory.from(principal);
        if (!scope.isPlatform()) {
            throw new PermissionAccessException(PermissionErrorCode.ROLE_ACCESS_DENIED);
        }
        if (!roleAssignmentRepository.platformMemberExists(platformMemberId)) {
            throw new PermissionAccessException(PermissionErrorCode.PLATFORM_MEMBER_UNAVAILABLE);
        }

        List<Role> roles = roleAccessService.requireAssignableRoles(request.getRoleIds(), scope);
        List<Long> roleIds = new LinkedHashSet<>(roles.stream().map(Role::getId).toList())
                .stream().toList();

        roleAssignmentRepository.replacePlatformRoles(platformMemberId, roleIds);
        permissionCache.invalidateAll();
        log.info("[PERMISSION] 平台成员角色分配 platformMemberId={} roleIds={}",
                platformMemberId, roleIds);
    }
}
