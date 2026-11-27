package com.kemall.iam.permission.infrastructure.redis;

import com.kemall.iam.permission.application.port.PermissionCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 基于 Redis 的权限缓存实现。
 * <p>
 * Key 设计：
 * <ul>
 *   <li>{@code iam:perm:version} —— 全局版本号，角色/权限变更时 INCR 整体失效</li>
 *   <li>{@code iam:perm:{version}:{sessionId}:{scope}:{context}} —— 权限编码 CSV</li>
 * </ul>
 * 空权限集用 {@code -} 作为占位，避免与"未缓存"混淆。
 */
@Component
@RequiredArgsConstructor
public class RedisPermissionCache implements PermissionCache {

    private static final String VERSION_KEY = "iam:perm:version";
    private static final String ENTRY_PREFIX = "iam:perm:";
    private static final String EMPTY_MARK = "-";

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<Set<String>> get(String cacheKey) {
        String raw = redisTemplate.opsForValue().get(entryKey(cacheKey));
        if (raw == null) {
            return Optional.empty();
        }
        if (EMPTY_MARK.equals(raw) || raw.isBlank()) {
            return Optional.of(Set.of());
        }
        return Optional.of(Arrays.stream(raw.split(","))
                .filter(code -> !code.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
    }

    @Override
    public void put(String cacheKey, Set<String> authorities, long ttlSeconds) {
        String value = authorities == null || authorities.isEmpty()
                ? EMPTY_MARK
                : String.join(",", authorities);
        redisTemplate.opsForValue().set(entryKey(cacheKey), value, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void evict(String cacheKey) {
        redisTemplate.delete(entryKey(cacheKey));
    }

    @Override
    public void invalidateAll() {
        redisTemplate.opsForValue().increment(VERSION_KEY);
    }

    private String entryKey(String cacheKey) {
        return ENTRY_PREFIX + currentVersion() + ":" + cacheKey;
    }

    private String currentVersion() {
        String version = redisTemplate.opsForValue().get(VERSION_KEY);
        return version == null ? "0" : version;
    }
}
