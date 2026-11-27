package com.kemall.iam.permission.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.api.enums.RoleScopeType;
import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.permission.domain.model.Permission;
import com.kemall.iam.permission.domain.model.Role;
import com.kemall.iam.permission.domain.repository.RoleRepository;
import com.kemall.iam.permission.infrastructure.persistence.mapper.RoleQueryMapper;
import com.kemall.iam.permission.infrastructure.persistence.mapper.RolePermissionMapper;
import com.kemall.iam.permission.domain.entity.RolePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 角色仓储实现：查询走自定义 XML，角色-权限写入走 MyBatis-Plus。
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleQueryMapper roleQueryMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Role> findByScope(Long tenantId, RoleScopeType scopeType) {
        return roleQueryMapper.selectByScope(tenantId, scopeType.getCode());
    }

    @Override
    public List<Role> findManageableRoles(WorkspaceType workspaceType, Long tenantId) {
        return roleQueryMapper.selectManageableRoles(workspaceType, tenantId);
    }

    @Override
    public Optional<Role> findById(Long roleId) {
        return Optional.ofNullable(roleQueryMapper.selectById(roleId));
    }

    @Override
    public List<Role> findByIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleQueryMapper.selectByIds(roleIds);
    }

    @Override
    public List<Permission> findPermissionsByRoleId(Long roleId) {
        return roleQueryMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        return roleQueryMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public void addPermissions(Long roleId, Collection<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Long permissionId : permissionIds) {
            RolePermission entity = new RolePermission()
                    .setRoleId(roleId)
                    .setPermissionId(permissionId);
            rolePermissionMapper.insert(entity);
        }
    }

    @Override
    public void removePermissions(Long roleId, Collection<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        rolePermissionMapper.delete(Wrappers.<RolePermission>lambdaQuery()
                .eq(RolePermission::getRoleId, roleId)
                .in(RolePermission::getPermissionId, permissionIds));
    }
}
