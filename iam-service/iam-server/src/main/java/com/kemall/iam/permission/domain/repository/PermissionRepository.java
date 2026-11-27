package com.kemall.iam.permission.domain.repository;

import com.kemall.iam.permission.domain.model.Permission;

import java.util.Collection;
import java.util.List;

/**
 * 权限字典仓储端口。
 */
public interface PermissionRepository {

    List<Permission> findAll();

    List<Permission> findByCodes(Collection<String> permCodes);

    List<Permission> findByRoleIds(Collection<Long> roleIds);
}
