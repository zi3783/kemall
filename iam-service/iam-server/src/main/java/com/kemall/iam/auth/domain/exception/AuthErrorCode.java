package com.kemall.iam.auth.domain.exception;


/**
 * 认证子域错误码
 */
public enum AuthErrorCode implements ErrorCode {

    // ===== 登录相关 40xxx =====
    LOGIN_FAILED(40001, "账号或密码错误"),

    // ===== 账号不可登录 401xx =====
    ACCOUNT_LOCKED(40101, "账号已被锁定，请联系管理员"),
    ACCOUNT_DISABLED(40102, "账号已被禁用"),
    ACCOUNT_EXPIRED(40103, "账号已过期"),
    ACCOUNT_NOT_ACTIVATED(40104, "账号尚未激活"),
    ACCOUNT_NOT_LOGIN_ALLOWED(40105, "当前账号不可登录"),  // 兜底

    // ===== 登录凭证 402xx =====
    TOKEN_EXPIRED(40201, "登录已过期，请重新登录"),
    TOKEN_INVALID(40202, "登录凭证无效"),
    ;

    private final Integer code;
    private final String message;

    AuthErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}