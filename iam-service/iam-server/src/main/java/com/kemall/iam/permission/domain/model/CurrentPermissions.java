package com.kemall.iam.permission.domain.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 当前用户在某工作空间下的有效角色与权限快照。
 */
public record CurrentPermissions(List<Role> roles, List<Permission> permissions) {

    public static CurrentPermissions empty() {
        return new CurrentPermissions(List.of(), List.of());
    }

    public List<Long> roleIds() {
        return roles.stream().map(Role::getId).toList();
    }

    public Set<String> permCodes() {
        return permissions.stream()
                .map(Permission::getPermCode)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
