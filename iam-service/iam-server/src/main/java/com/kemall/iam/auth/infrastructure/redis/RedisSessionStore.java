package com.kemall.iam.auth.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.domain.exception.SessionInvalidException;
import com.kemall.iam.auth.domain.model.AuthSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 基于 Redis 的会话存储实现。
 * <p>
 * 两类 key：
 * <ul>
 *   <li>{@code iam:session:{sessionId}} —— 会话主体，String(JSON)，TTL = refresh 有效期</li>
 *   <li>{@code iam:user:sessions:{userId}} —— 用户会话索引，Set，元素 = sessionId，TTL 同主会话</li>
 * </ul>
 * 会话创建/撤销/续期都同步维护用户索引，
 * 以便"全部退出""设备列表""改密撤销其他"等场景快速定位。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSessionStore implements SessionStore {

    private static final String SESSION_PREFIX = "iam:session:";
    private static final String USER_INDEX_PREFIX = "iam:user:sessions:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void createSession(AuthSession session) {
        String sessionKey = sessionKey(session.getSessionId());
        String indexKey = userIndexKey(session.getUserId());
        save(sessionKey, session);
        redisTemplate.opsForSet().add(indexKey, session.getSessionId());
        syncIndexTtl(indexKey, session);
    }

    @Override
    public AuthSession getSession(String sessionId) {
        String json = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, AuthSession.class);
        } catch (JsonProcessingException e) {
            log.error("读取 Session 反序列化失败 sessionId={}", sessionId, e);
            return null;
        }
    }

    @Override
    public void updateSession(AuthSession session) {
        save(sessionKey(session.getSessionId()), session);
        syncIndexTtl(userIndexKey(session.getUserId()), session);
    }

    @Override
    public void revokeSession(String sessionId) {
        AuthSession session = getSession(sessionId);
        redisTemplate.delete(sessionKey(sessionId));
        if (session != null) {
            String indexKey = userIndexKey(session.getUserId());
            redisTemplate.opsForSet().remove(indexKey, sessionId);
            // set 空了就删 index key
            Long size = redisTemplate.opsForSet().size(indexKey);
            if (size != null && size == 0) {
                redisTemplate.delete(indexKey);
            }
        }
    }

    @Override
    public boolean exists(String sessionId) {
        AuthSession session = getSession(sessionId);
        if (session == null || session.isRevoked()) return false;
        return session.getExpiresAt() != null
                && session.getExpiresAt().isAfter(LocalDateTime.now());
    }

    @Override
    public void saveRefreshTokenHash(String sessionId, String refreshToken) {
        AuthSession session = getSession(sessionId);
        if (session == null || session.isRevoked()) {
            throw new SessionInvalidException();
        }
        session.setRefreshTokenHash(sha256(refreshToken));
        session.setLastActiveAt(LocalDateTime.now());
        updateSession(session);
    }

    @Override
    public boolean matchesRefreshToken(String sessionId, String refreshToken) {
        AuthSession session = getSession(sessionId);
        if (session == null
                || session.isRevoked()
                || session.getRefreshTokenHash() == null) {
            return false;
        }
        String actual = sha256(refreshToken);
        return MessageDigest.isEqual(
                session.getRefreshTokenHash().getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<AuthSession> listSessions(Long userId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(userIndexKey(userId));
        if (sessionIds == null || sessionIds.isEmpty()) return Collections.emptyList();

        List<AuthSession> result = new ArrayList<>(sessionIds.size());
        for (String sid : sessionIds) {
            AuthSession s = getSession(sid);
            if (s == null) continue;
            // 过滤脏索引（已撤销/过期的），不阻塞返回
            if (s.isRevoked()
                    || s.getExpiresAt() == null
                    || s.getExpiresAt().isBefore(LocalDateTime.now())) {
                continue;
            }
            result.add(s);
        }
        return result;
    }

    @Override
    public void revokeAllSessions(Long userId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(userIndexKey(userId));
        if (sessionIds == null || sessionIds.isEmpty()) return;
        for (String sid : sessionIds) {
            redisTemplate.delete(sessionKey(sid));
        }
        redisTemplate.delete(userIndexKey(userId));
    }

    @Override
    public void revokeOtherSessions(Long userId, String keepSessionId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(userIndexKey(userId));
        if (sessionIds == null || sessionIds.isEmpty()) return;
        for (String sid : sessionIds) {
            if (keepSessionId != null && keepSessionId.equals(sid)) continue;
            redisTemplate.delete(sessionKey(sid));
        }
        redisTemplate.opsForSet().remove(userIndexKey(userId), sessionIds.toArray());
        if (keepSessionId != null) {
            redisTemplate.opsForSet().add(userIndexKey(userId), keepSessionId);
        } else {
            redisTemplate.delete(userIndexKey(userId));
        }
    }

    @Override
    public void removeFromUserIndex(Long userId, String sessionId) {
        redisTemplate.opsForSet().remove(userIndexKey(userId), sessionId);
    }

    // ===== 内部工具 =====

    private void save(String sessionKey, AuthSession session) {
        long seconds = Duration.between(LocalDateTime.now(), session.getExpiresAt()).getSeconds();
        if (seconds <= 0) {
            throw new IllegalArgumentException("Session 已过期，无法保存");
        }
        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(sessionKey, json, Duration.ofSeconds(seconds));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("保存 Session 序列化失败", e);
        }
    }

    private void syncIndexTtl(String indexKey, AuthSession session) {
        long seconds = Duration.between(LocalDateTime.now(), session.getExpiresAt()).getSeconds();
        if (seconds > 0) {
            redisTemplate.expire(indexKey, Duration.ofSeconds(seconds));
        }
    }

    private String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    private String userIndexKey(Long userId) {
        return USER_INDEX_PREFIX + userId;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }
}
