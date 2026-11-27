package com.kemall.iam.tenancy.infrastructure.security;

import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.tenancy.domain.port.MembershipSessionRevoker;
import com.kemall.iam.tenancy.infrastructure.persistence.mapper.TenantMemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 成员会话撤销适配器：租户冻结、店铺停用、成员禁用/移除后立即失效相关会话。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenancySessionRevoker implements MembershipSessionRevoker {

    private final SessionStore sessionStore;
    private final TenantMemberMapper tenantMemberMapper;

    @Override
    public void revokeMember(Long userId, Long memberId) {
        revokeUserSessions(userId, session -> Objects.equals(session.getMemberId(), memberId));
    }

    @Override
    public void revokeShop(Long tenantId, Long shopId) {
        List<Long> userIds = tenantMemberMapper.selectUserIdsByShop(tenantId, shopId);
        for (Long userId : userIds) {
            revokeUserSessions(userId, session ->
                    Objects.equals(session.getTenantId(), tenantId)
                            && Objects.equals(session.getShopId(), shopId));
        }
    }

    @Override
    public void revokeTenant(Long tenantId) {
        List<Long> userIds = tenantMemberMapper.selectUserIdsByTenant(tenantId);
        for (Long userId : userIds) {
            revokeUserSessions(userId, session -> Objects.equals(session.getTenantId(), tenantId));
        }
    }

    private void revokeUserSessions(Long userId, Predicate<AuthSession> filter) {
        List<AuthSession> sessions;
        try {
            sessions = sessionStore.listSessions(userId);
        } catch (Exception e) {
            log.warn("[TENANCY] 读取用户会话失败 userId={}", userId, e);
            return;
        }
        for (AuthSession session : sessions) {
            if (!filter.test(session)) {
                continue;
            }
            try {
                sessionStore.revokeSession(session.getSessionId());
            } catch (Exception e) {
                log.warn("[TENANCY] 撤销会话失败 sessionId={}", session.getSessionId(), e);
            }
        }
    }
}
