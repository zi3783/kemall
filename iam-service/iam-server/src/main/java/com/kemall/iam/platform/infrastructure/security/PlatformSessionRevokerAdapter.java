package com.kemall.iam.platform.infrastructure.security;

import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.platform.domain.port.PlatformSessionRevoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 平台成员会话撤销适配器：禁用平台成员后，其平台空间会话立即失效。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformSessionRevokerAdapter implements PlatformSessionRevoker {

    private final SessionStore sessionStore;

    @Override
    public void revoke(Long userId, Long platformMemberId) {
        List<AuthSession> sessions;
        try {
            sessions = sessionStore.listSessions(userId);
        } catch (Exception e) {
            log.warn("[PLATFORM] 读取用户会话失败 userId={}", userId, e);
            return;
        }
        for (AuthSession session : sessions) {
            if (!Objects.equals(session.getPlatformMemberId(), platformMemberId)) {
                continue;
            }
            try {
                sessionStore.revokeSession(session.getSessionId());
            } catch (Exception e) {
                log.warn("[PLATFORM] 撤销会话失败 sessionId={}", session.getSessionId(), e);
            }
        }
    }
}
