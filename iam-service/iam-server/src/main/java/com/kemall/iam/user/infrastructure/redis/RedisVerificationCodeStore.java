package com.kemall.iam.user.infrastructure.redis;

import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;
import com.kemall.iam.user.application.port.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Redis 的验证码存储实现。
 * <p>
 * Key 设计：
 * <ul>
 *   <li>{@code iam:code:{scene}:{type}:{target}} —— 验证码，TTL = 有效期</li>
 *   <li>{@code iam:code:attempt:{scene}:{type}:{target}} —— 错误次数</li>
 *   <li>{@code iam:code:limit:{type}:{target}} —— 发送频控（SET NX EX）</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisVerificationCodeStore implements VerificationCodeStore {

    private static final String CODE_PREFIX = "iam:code:";
    private static final String ATTEMPT_PREFIX = "iam:code:attempt:";
    private static final String LIMIT_PREFIX = "iam:code:limit:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(VerificationScene scene, IdentityType type, String target, String code, long ttlSeconds) {
        redisTemplate.opsForValue().set(codeKey(scene, type, target), code, Duration.ofSeconds(ttlSeconds));
        redisTemplate.delete(attemptKey(scene, type, target));
    }

    @Override
    public boolean verify(VerificationScene scene, IdentityType type, String target, String code, int maxAttempts) {
        String key = codeKey(scene, type, target);
        String expected = redisTemplate.opsForValue().get(key);
        if (expected == null) {
            return false;
        }
        if (expected.equals(code)) {
            return true;
        }

        String attemptKey = attemptKey(scene, type, target);
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1) {
            Long ttl = redisTemplate.getExpire(key);
            if (ttl != null && ttl > 0) {
                redisTemplate.expire(attemptKey, Duration.ofSeconds(ttl));
            }
        }
        if (attempts != null && attempts >= maxAttempts) {
            // 超过上限作废验证码，需重新获取
            redisTemplate.delete(key);
            redisTemplate.delete(attemptKey);
            log.warn("[USER] 验证码错误次数超限已作废 scene={} type={} target={}", scene, type, target);
        }
        return false;
    }

    @Override
    public void consume(VerificationScene scene, IdentityType type, String target) {
        redisTemplate.delete(codeKey(scene, type, target));
        redisTemplate.delete(attemptKey(scene, type, target));
    }

    @Override
    public boolean tryAcquireSendQuota(IdentityType type, String target, long seconds) {
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(LIMIT_PREFIX + type.name() + ":" + target, "1", Duration.ofSeconds(seconds));
        return Boolean.TRUE.equals(ok);
    }

    private String codeKey(VerificationScene scene, IdentityType type, String target) {
        return CODE_PREFIX + scene.name() + ":" + type.name() + ":" + target;
    }

    private String attemptKey(VerificationScene scene, IdentityType type, String target) {
        return ATTEMPT_PREFIX + scene.name() + ":" + type.name() + ":" + target;
    }
}
