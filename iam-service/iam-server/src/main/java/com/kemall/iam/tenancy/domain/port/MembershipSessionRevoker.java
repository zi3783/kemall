package com.kemall.iam.tenancy.domain.port;

/**
 * 成员相关会话撤销端口。
 * <p>
 * 由基础设施适配 auth 的 SessionStore 实现，用于租户冻结、店铺停用、
 * 成员禁用/移除后立即让相关在线会话失效。
 */
public interface MembershipSessionRevoker {

    void revokeMember(Long userId, Long memberId);

    void revokeShop(Long tenantId, Long shopId);

    void revokeTenant(Long tenantId);
}
