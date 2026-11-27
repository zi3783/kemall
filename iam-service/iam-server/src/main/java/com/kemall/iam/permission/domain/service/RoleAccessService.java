package com.kemall.iam.permission.domain.service;

import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.permission.domain.exception.PermissionAccessException;
import com.kemall.iam.permission.domain.exception.PermissionErrorCode;
import com.kemall.iam.permission.domain.exception.RoleNotFoundException;
import com.kemall.iam.permission.domain.model.PermissionScope;
import com.kemall.iam.permission.domain.model.Role;
import com.kemall.iam.permission.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色可管理边界领域服务（domain boundary）。
 * <p>
 * 本服务只判定"角色是否在当前工作空间的管辖范围内"，
 * 不判定"当前用户有没有做角色管理这件事的权限"——后者由
 * Controller 上的 {@code @PreAuthorize} 负责。
 * <p>
 * 层级边界规则（scope_type：0=平台顶层, 1=租户中层, 2=店铺底层）：
 * <ul>
 *   <li>Platform workspace → 管 scope=0 且 tenant=0 的平台角色</li>
 *   <li>Tenant workspace   → 管 scope≥1（租户+店铺角色）且 tenant_id=自己的 tenantId</li>
 *   <li>Shop workspace     → 管 scope=2 且 tenant_id=自己的 tenantId 的店铺角色</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class RoleAccessService {

    private final RoleRepository roleRepository;

    /**
     * 读取并校验角色在当前工作空间管辖范围内。
     */
    public Role requireRole(Long roleId, PermissionScope scope) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        ensureManageable(role, scope);
        if (!role.isEnabled()) {
            throw new PermissionAccessException(PermissionErrorCode.ROLE_ACCESS_DENIED);
        }
        return role;
    }

    /**
     * 校验一批待分配角色均在当前工作空间管辖范围内。
     */
    public List<Role> requireAssignableRoles(Collection<Long> roleIds, PermissionScope scope) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        Set<Long> distinct = new HashSet<>(roleIds);
        List<Role> roles = roleRepository.findByIds(distinct);
        if (roles.size() != distinct.size()) {
            throw new RoleNotFoundException(null);
        }
        roles.forEach(role -> {
            ensureManageable(role, scope);
            if (!role.isEnabled()) {
                throw new PermissionAccessException(PermissionErrorCode.ROLE_ACCESS_DENIED);
            }
        });
        return roles;
    }

    /**
     * 角色在当前工作空间的层级管辖范围内吗？
     */
    public boolean isManageable(Role role, PermissionScope scope) {
        if (scope == null || scope.isEmpty() || scope.workspaceType() == null
                || role == null || role.getScopeType() == null || role.getTenantId() == null) {
            return false;
        }
        return switch (scope.workspaceType()) {
            case PLATFORM -> role.getScopeType() >= 0;
            case TENANT -> role.getTenantId().equals(scope.tenantId()) && role.getScopeType() >= 1;
            case SHOP -> role.getTenantId().equals(scope.tenantId()) && role.getScopeType() == 2;
            default -> false;
        };
    }

    /**
     * 层级管辖边界校验（域边界 ≠ 授权）。
     *
     * @see #isManageable(Role, PermissionScope)
     */
    public void ensureManageable(Role role, PermissionScope scope) {
        if (scope == null || scope.isEmpty() || scope.workspaceType() == null) {
            throw new PermissionAccessException(PermissionErrorCode.ROLE_ACCESS_DENIED);
        }
        if (!isManageable(role, scope)) {
            throw new PermissionAccessException(PermissionErrorCode.ROLE_SCOPE_MISMATCH);
        }
    }
}
