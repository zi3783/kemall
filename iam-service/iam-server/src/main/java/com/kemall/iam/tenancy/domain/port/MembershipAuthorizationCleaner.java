package com.kemall.iam.tenancy.domain.port;

/**
 * 成员授权清理端口。
 * <p>
 * 由 permission 子域实现（清理 member_role），避免租户子域反向依赖权限子域。
 */
public interface MembershipAuthorizationCleaner {

    void clearMemberRoles(Long tenantId, Long memberId);
}
