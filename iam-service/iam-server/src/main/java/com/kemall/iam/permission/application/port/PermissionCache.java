package com.kemall.iam.permission.application.port;

import java.util.Optional;
import java.util.Set;

/**
 * 当前权限缓存端口。
 * <p>
 * 以"会话 + 工作空间上下文"为 key 缓存权限编码集合，
 * 角色/权限变更时通过版本号整体失效。
 */
public interface PermissionCache {

    Optional<Set<String>> get(String cacheKey);

    void put(String cacheKey, Set<String> authorities, long ttlSeconds);

    void evict(String cacheKey);

    /**
     * 角色/权限配置变更后整体失效。
     */
    void invalidateAll();
}
