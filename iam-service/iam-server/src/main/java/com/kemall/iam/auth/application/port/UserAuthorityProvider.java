package com.kemall.iam.auth.application.port;

import com.kemall.iam.auth.domain.model.AuthSession;

import java.util.Collection;

/**
 * 当前会话权限编码提供者。
 * <p>
 * 由下游权限子域实现，JWT 过滤器通过该端口装载 Spring Security Authorities，
 * 避免认证基础设施反向依赖权限实现。
 */
public interface UserAuthorityProvider {

    /**
     * 加载当前工作空间下的权限编码集合。
     */
    Collection<String> loadAuthorities(AuthSession session);
}
