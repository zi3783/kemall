package com.kemall.iam.permission.infrastructure.persistence.mapper;

import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.permission.domain.model.Permission;
import com.kemall.iam.permission.domain.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 角色查询 Mapper（返回领域对象）。
 */
@Mapper
public interface RoleQueryMapper {

    List<Role> selectByScope(@Param("tenantId") Long tenantId,
                             @Param("scopeType") Integer scopeType);

    /**
     * 按工作空间层级边界查询可管理角色。
     */
    List<Role> selectManageableRoles(@Param("workspaceType") WorkspaceType workspaceType,
                                     @Param("tenantId") Long tenantId);

    Role selectById(@Param("roleId") Long roleId);

    List<Role> selectByIds(@Param("roleIds") Collection<Long> roleIds);

    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
