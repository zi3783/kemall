package com.kemall.iam.permission.application.support;

import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.permission.domain.model.PermissionScope;

/**
 * 从登录主体/会话推导权限作用域（与工作空间上下文保持一致）。
 */
public final class PermissionScopeFactory {

    private PermissionScopeFactory() {
    }

    public static PermissionScope from(LoginPrincipal principal) {
        return resolve(principal.getTenantId(), principal.getShopId(),
                principal.getMemberId(), principal.getPlatformMemberId());
    }

    public static PermissionScope from(AuthSession session) {
        return resolve(session.getTenantId(), session.getShopId(),
                session.getMemberId(), session.getPlatformMemberId());
    }

    private static PermissionScope resolve(Long tenantId, Long shopId,
                                           Long memberId, Long platformMemberId) {
        if (platformMemberId != null) {
            return new PermissionScope(WorkspaceType.PLATFORM, 0L, null, platformMemberId);
        }
        if (shopId != null && shopId > 0) {
            return new PermissionScope(WorkspaceType.SHOP, tenantId, memberId, null);
        }
        if (tenantId != null) {
            return new PermissionScope(WorkspaceType.TENANT, tenantId, memberId, null);
        }
        return new PermissionScope(WorkspaceType.NONE, null, null, null);
    }
}
