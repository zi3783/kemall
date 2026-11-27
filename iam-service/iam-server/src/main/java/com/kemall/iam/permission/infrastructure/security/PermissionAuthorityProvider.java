package com.kemall.iam.permission.infrastructure.security;

import com.kemall.iam.auth.application.port.UserAuthorityProvider;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.permission.application.service.PermissionAppService;
import com.kemall.iam.permission.application.support.PermissionScopeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * {@link UserAuthorityProvider} 的权限子域实现。
 * <p>
 * 认证侧只依赖端口，权限解析与缓存逻辑收敛在 permission 子域。
 */
@Component
@RequiredArgsConstructor
public class PermissionAuthorityProvider implements UserAuthorityProvider {

    private final PermissionAppService permissionAppService;

    @Override
    public Collection<String> loadAuthorities(AuthSession session) {
        return permissionAppService.loadAuthorities(
                session.getSessionId(),
                PermissionScopeFactory.from(session));
    }
}
