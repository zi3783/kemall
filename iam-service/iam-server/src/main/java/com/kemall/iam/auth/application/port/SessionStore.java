package com.kemall.iam.auth.application.port;

import com.kemall.iam.auth.domain.model.AuthSession;

import java.util.List;

/**
 * 会话存储端口（Repository Port）。
 * <p>
 * 领域层只关心会话生命周期的业务语义，
 * 不关心底层是 Redis 还是其他存储。
 */
public interface SessionStore {

    void createSession(AuthSession session);

    AuthSession getSession(String sessionId);

    void updateSession(AuthSession session);

    void revokeSession(String sessionId);

    boolean exists(String sessionId);

    /**
     * 保存 Refresh Token 哈希
     */
    void saveRefreshTokenHash(String sessionId, String refreshToken);

    /**
     * 校验 Refresh Token
     */
    boolean matchesRefreshToken(String sessionId, String refreshToken);

    /**
     * 列出指定用户的所有有效会话（含已失效的，由应用服务过滤）
     */
    List<AuthSession> listSessions(Long userId);

    /**
     * 撤销某用户的全部会话
     */
    void revokeAllSessions(Long userId);

    /**
     * 撤销某用户除保留会话外的全部会话
     */
    void revokeOtherSessions(Long userId, String keepSessionId);

    /**
     * 从用户索引中移除指定会话
     */
    void removeFromUserIndex(Long userId, String sessionId);
}
