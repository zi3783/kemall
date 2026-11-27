package com.kemall.iam.permission.domain.repository;

import com.kemall.iam.api.enums.RoleScopeType;
import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.permission.domain.model.Permission;
import com.kemall.iam.permission.domain.model.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 角色仓储端口。角色与角色-权限关联同属一个聚合。
 */
public interface RoleRepository {

    List<Role> findByScope(Long tenantId, RoleScopeType scopeType);

    /**
     * 当前工作空间层级管辖范围内的启用角色（用于角色列表）。
     *
     * @param workspaceType 当前工作空间类型
     * @param tenantId      租户/店铺工作空间的租户 ID（平台空间为 null）
     */
    List<Role> findManageableRoles(WorkspaceType workspaceType, Long tenantId);

    Optional<Role> findById(Long roleId);

    List<Role> findByIds(Collection<Long> roleIds);

    List<Permission> findPermissionsByRoleId(Long roleId);

    List<Long> findPermissionIdsByRoleId(Long roleId);

    void addPermissions(Long roleId, Collection<Long> permissionIds);

    void removePermissions(Long roleId, Collection<Long> permissionIds);
}
