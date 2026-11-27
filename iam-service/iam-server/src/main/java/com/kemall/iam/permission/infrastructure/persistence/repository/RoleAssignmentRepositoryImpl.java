package com.kemall.iam.permission.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.api.enums.RoleScopeType;
import com.kemall.iam.permission.domain.entity.MemberRole;
import com.kemall.iam.permission.domain.model.Role;
import com.kemall.iam.permission.domain.repository.RoleAssignmentRepository;
import com.kemall.iam.permission.infrastructure.persistence.mapper.RoleAssignmentQueryMapper;
import com.kemall.iam.permission.infrastructure.persistence.mapper.MemberRoleMapper;
import com.kemall.iam.permission.domain.entity.PlatformMemberRole;
import com.kemall.iam.permission.infrastructure.persistence.mapper.PlatformMemberRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 成员-角色分配仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RoleAssignmentRepositoryImpl implements RoleAssignmentRepository {

    private final RoleAssignmentQueryMapper roleAssignmentQueryMapper;
    private final MemberRoleMapper memberRoleMapper;
    private final PlatformMemberRoleMapper platformMemberRoleMapper;

    @Override
    public List<Role> findMemberRoles(Long tenantId, Long memberId, RoleScopeType scopeType) {
        return roleAssignmentQueryMapper.selectMemberRoles(tenantId, memberId, scopeType.getCode());
    }

    @Override
    public List<Role> findPlatformRoles(Long platformMemberId) {
        return roleAssignmentQueryMapper.selectPlatformRoles(platformMemberId);
    }

    @Override
    public boolean tenantMemberExists(Long tenantId, Long memberId) {
        Long count = roleAssignmentQueryMapper.countActiveTenantMember(tenantId, memberId);
        return count != null && count > 0;
    }

    @Override
    public boolean platformMemberExists(Long platformMemberId) {
        Long count = roleAssignmentQueryMapper.countActivePlatformMember(platformMemberId);
        return count != null && count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceMemberRoles(Long tenantId, Long memberId, Collection<Long> roleIds) {
        memberRoleMapper.delete(Wrappers.<MemberRole>lambdaQuery()
                .eq(MemberRole::getTenantId, tenantId)
                .eq(MemberRole::getMemberId, memberId));
        for (Long roleId : roleIds) {
            MemberRole entity = new MemberRole()
                    .setTenantId(tenantId)
                    .setMemberId(memberId)
                    .setRoleId(roleId);
            memberRoleMapper.insert(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePlatformRoles(Long platformMemberId, Collection<Long> roleIds) {
        platformMemberRoleMapper.delete(Wrappers.<PlatformMemberRole>lambdaQuery()
                .eq(PlatformMemberRole::getPlatformMemberId, platformMemberId));
        for (Long roleId : roleIds) {
            PlatformMemberRole entity = new PlatformMemberRole()
                    .setPlatformMemberId(platformMemberId)
                    .setRoleId(roleId);
            platformMemberRoleMapper.insert(entity);
        }
    }
}
