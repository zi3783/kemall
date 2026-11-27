package com.kemall.iam.permission.domain.repository;

import com.kemall.iam.api.enums.RoleScopeType;
import com.kemall.iam.permission.domain.model.Role;

import java.util.Collection;
import java.util.List;

/**
 * 成员-角色分配仓储端口。
 * <p>
 * 租户/店铺成员走 member_role，平台成员走 platform_member_role，
 * 两条链路互不混用（平台角色约定）。
 */
public interface RoleAssignmentRepository {

    /**
     * 租户/店铺成员的有效角色（按角色作用域过滤）。
     */
    List<Role> findMemberRoles(Long tenantId, Long memberId, RoleScopeType scopeType);

    /**
     * 平台成员的有效平台角色。
     */
    List<Role> findPlatformRoles(Long platformMemberId);

    boolean tenantMemberExists(Long tenantId, Long memberId);

    boolean platformMemberExists(Long platformMemberId);

    void replaceMemberRoles(Long tenantId, Long memberId, Collection<Long> roleIds);

    void replacePlatformRoles(Long platformMemberId, Collection<Long> roleIds);
}
