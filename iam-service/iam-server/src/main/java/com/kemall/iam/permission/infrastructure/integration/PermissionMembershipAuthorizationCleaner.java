package com.kemall.iam.permission.infrastructure.integration;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kemall.iam.permission.domain.entity.MemberRole;
import com.kemall.iam.permission.infrastructure.persistence.mapper.MemberRoleMapper;
import com.kemall.iam.tenancy.domain.port.MembershipAuthorizationCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 成员授权清理端口实现：成员移除时清空 member_role。
 */
@Component
@RequiredArgsConstructor
public class PermissionMembershipAuthorizationCleaner implements MembershipAuthorizationCleaner {

    private final MemberRoleMapper memberRoleMapper;

    @Override
    public void clearMemberRoles(Long tenantId, Long memberId) {
        memberRoleMapper.delete(Wrappers.<MemberRole>lambdaQuery()
                .eq(MemberRole::getTenantId, tenantId)
                .eq(MemberRole::getMemberId, memberId));
    }
}
