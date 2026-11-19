package com.kemall.iam.auth.application.port;

import com.kemall.iam.auth.domain.model.AuthSession;

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
}