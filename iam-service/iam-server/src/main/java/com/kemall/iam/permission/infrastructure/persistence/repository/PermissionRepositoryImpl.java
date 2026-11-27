package com.kemall.iam.permission.infrastructure.persistence.repository;

import com.kemall.iam.permission.domain.model.Permission;
import com.kemall.iam.permission.domain.repository.PermissionRepository;
import com.kemall.iam.permission.infrastructure.persistence.mapper.PermissionQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 权限字典仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionQueryMapper permissionQueryMapper;

    @Override
    public List<Permission> findAll() {
        return permissionQueryMapper.selectAll();
    }

    @Override
    public List<Permission> findByCodes(Collection<String> permCodes) {
        if (permCodes == null || permCodes.isEmpty()) {
            return List.of();
        }
        return permissionQueryMapper.selectByCodes(permCodes);
    }

    @Override
    public List<Permission> findByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return permissionQueryMapper.selectByRoleIds(roleIds);
    }
}
