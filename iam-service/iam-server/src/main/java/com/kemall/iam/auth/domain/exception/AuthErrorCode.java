package com.kemall.iam.auth.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 认证子域错误码。
 * <p>
 * 码段约定：
 * 400xx — 登录/参数类业务失败
 * 401xx — 账号状态类
 * 402xx — Token / Session 类
 * 403xx — 权限类
 * 404xx — 业务资源不存在
 * 410xx — 密码/账户操作
 */
public enum AuthErrorCode implements ErrorCode {

    // ===== 登录相关 400xx =====
    LOGIN_FAILED(40001, "账号或密码错误"),

    // ===== 账号不可登录 401xx =====
    ACCOUNT_LOCKED(40101, "账号已被锁定，请联系管理员"),
    ACCOUNT_DISABLED(40102, "账号已被禁用"),
    ACCOUNT_EXPIRED(40103, "账号已过期"),
    ACCOUNT_NOT_ACTIVATED(40104, "账号尚未激活"),
    ACCOUNT_NOT_LOGIN_ALLOWED(40105, "当前账号不可登录"),

    // ===== 未登录 40100 =====
    NOT_AUTHENTICATED(40100, "未登录或登录已过期"),

    // ===== 登录凭证 402xx =====
    TOKEN_EXPIRED(40201, "登录已过期，请重新登录"),
    TOKEN_INVALID(40202, "登录凭证无效"),
    TOKEN_PURPOSE_INVALID(40203, "凭证用途不合法"),
    SESSION_INVALID(40204, "会话不存在或已失效"),
    REFRESH_TOKEN_REUSED(40205, "刷新凭证已失效，疑似重放"),
    REFRESH_BUSY(40206, "刷新请求处理中，请稍后重试"),

    // ===== 权限 403xx =====
    PERMISSION_DENIED(40300, "无操作权限"),

    // ===== 密码操作 410xx =====
    PASSWORD_MISMATCH(41001, "原密码错误"),
    PASSWORD_SAME_AS_OLD(41002, "新密码不能与原密码相同"),
    USER_NOT_FOUND(41003, "用户不存在"),
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
