package com.kemall.iam.permission.infrastructure.persistence.mapper;

import com.kemall.iam.permission.domain.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 权限字典查询 Mapper（返回领域对象）。
 */
@Mapper
public interface PermissionQueryMapper {

    List<Permission> selectAll();

    List<Permission> selectByCodes(@Param("permCodes") Collection<String> permCodes);

    List<Permission> selectByRoleIds(@Param("roleIds") Collection<Long> roleIds);
}
