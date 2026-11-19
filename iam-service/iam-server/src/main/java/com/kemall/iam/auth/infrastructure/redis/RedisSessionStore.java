
package com.kemall.iam.auth.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.domain.model.AuthSession;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class RedisSessionStore implements SessionStore {

    private static final String SESSION_PREFIX = "iam:session:";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public RedisSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void createSession(AuthSession session) {
        save(session);
    }

    @Override
    public AuthSession getSession(String sessionId) {

        String json = redisTemplate.opsForValue().get(
                SESSION_PREFIX + sessionId
        );

        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, AuthSession.class);
        } catch (Exception e) {
            throw new IllegalStateException("读取 Session 失败", e);
        }
    }

    @Override
    public void updateSession(AuthSession session) {
        save(session);
    }

    private void save(AuthSession session) {

        long seconds = Duration.between(
                LocalDateTime.now(),
                session.getExpiresAt()
        ).getSeconds();

        if (seconds <= 0) {
            throw new IllegalArgumentException("Session 已过期");
        }

        try {
            String json = objectMapper.writeValueAsString(session);

            redisTemplate.opsForValue().set(
                    SESSION_PREFIX + session.getSessionId(),
                    json,
                    Duration.ofSeconds(seconds)
            );

        } catch (Exception e) {
            throw new IllegalStateException("保存 Session 失败", e);
        }
    }

    @Override
    public void revokeSession(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }

    @Override
    public boolean exists(String sessionId) {

        AuthSession session = getSession(sessionId);

        return session != null
                && !session.isRevoked()
                && session.getExpiresAt() != null
                && session.getExpiresAt().isAfter(LocalDateTime.now());
    }

    @Override
    public void saveRefreshTokenHash(
            String sessionId,
            String refreshToken) {

        AuthSession session = getSession(sessionId);

        if (session == null || session.isRevoked()) {
            throw new IllegalStateException("Session 不存在或已撤销");
        }

        session.setRefreshTokenHash(sha256(refreshToken));

        updateSession(session);
    }

    @Override
    public boolean matchesRefreshToken(
            String sessionId,
            String refreshToken) {

        AuthSession session = getSession(sessionId);

        if (session == null
                || session.isRevoked()
                || session.getRefreshTokenHash() == null) {
            return false;
        }

        String actualHash = sha256(refreshToken);

        return MessageDigest.isEqual(
                session.getRefreshTokenHash()
                        .getBytes(StandardCharsets.UTF_8),
                actualHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String sha256(String value) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new IllegalStateException("Refresh Token 哈希计算失败", e);
        }
    }
}
