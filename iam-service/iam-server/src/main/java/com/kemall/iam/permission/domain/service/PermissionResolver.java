package com.kemall.iam.permission.domain.service;

import com.kemall.iam.permission.domain.model.CurrentPermissions;
import com.kemall.iam.permission.domain.model.Permission;
import com.kemall.iam.permission.domain.model.PermissionScope;
import com.kemall.iam.permission.domain.model.Role;
import com.kemall.iam.permission.domain.repository.PermissionRepository;
import com.kemall.iam.permission.domain.repository.RoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 有效权限解析领域服务。
 * <p>
 * 平台空间：platform_member_role → 平台角色 → role_permission；
 * 租户/店铺空间：member_role → 对应作用域角色 → role_permission。
 */
@Component
@RequiredArgsConstructor
public class PermissionResolver {

    private final RoleAssignmentRepository roleAssignmentRepository;
    private final PermissionRepository permissionRepository;

    public CurrentPermissions resolve(PermissionScope scope) {
        if (scope == null || scope.isEmpty()) {
            return CurrentPermissions.empty();
        }

        List<Role> roles;
        if (scope.isPlatform()) {
            if (scope.platformMemberId() == null) {
                return CurrentPermissions.empty();
            }
            roles = roleAssignmentRepository.findPlatformRoles(scope.platformMemberId());
        } else {
            if (scope.tenantId() == null || scope.memberId() == null || scope.roleScopeType() == null) {
                return CurrentPermissions.empty();
            }
            roles = roleAssignmentRepository.findMemberRoles(
                    scope.tenantId(), scope.memberId(), scope.roleScopeType());
        }

        if (roles.isEmpty()) {
            return CurrentPermissions.empty();
        }

        List<Long> roleIds = roles.stream().map(Role::getId).toList();
        List<Permission> permissions = permissionRepository.findByRoleIds(roleIds);
        return new CurrentPermissions(roles, permissions);
    }
}
