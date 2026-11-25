package com.kemall.iam.auth.domain.exception;

import java.util.Map;

/**
 * 账号不可登录异常
 * <p>
 * 根据账号状态（锁定/禁用/过期/未激活）区分具体原因，
 * 便于前端展示更精准的提示与引导（如引导用户激活、联系管理员）。
 */
public class AccountNotLoginAllowedException extends AuthDomainException {

    public AccountNotLoginAllowedException(Long userId, String status) {
        super(resolveErrorCode(status),
                Map.of("userId", userId, "status", status));
    }

    private static AuthErrorCode resolveErrorCode(String status) {
        if (status == null) {
            return AuthErrorCode.ACCOUNT_NOT_LOGIN_ALLOWED;
        }
        switch (status) {
            case "LOCKED":        return AuthErrorCode.ACCOUNT_LOCKED;
            case "DISABLED":      return AuthErrorCode.ACCOUNT_DISABLED;
            case "EXPIRED":       return AuthErrorCode.ACCOUNT_EXPIRED;
            case "NOT_ACTIVATED": return AuthErrorCode.ACCOUNT_NOT_ACTIVATED;
            default:              return AuthErrorCode.ACCOUNT_NOT_LOGIN_ALLOWED;
        }

    }
}