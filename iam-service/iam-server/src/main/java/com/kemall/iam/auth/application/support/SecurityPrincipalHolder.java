package com.kemall.iam.auth.application.support;

import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.auth.domain.exception.PermissionDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文便捷工具。
 * <p>
 * 应用服务层通过它从 SecurityContext 取当前登录主体，
 * 避免每个用例代码散落 {@code SecurityContextHolder.getContext().getAuthentication()}。
 */
public final class SecurityPrincipalHolder {

    private SecurityPrincipalHolder() {}

    public static LoginPrincipal current() {
        Object auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth instanceof LoginPrincipal lp)) {
            throw new PermissionDeniedException();
        }
        return lp;
    }

    public static Long currentUserId() {
        return current().getUserId();
    }

    public static String currentSessionId() {
        return current().getSessionId();
    }
}
