package com.kemall.iam.user.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 用户子域错误码。
 * <p>
 * 码段约定：420xx 用户账号/资料/身份绑定。
 */
public enum UserErrorCode implements ErrorCode {

    USERNAME_EXISTS(42001, "用户名已存在"),
    MOBILE_ALREADY_BOUND(42002, "手机号已绑定其他账号"),
    EMAIL_ALREADY_BOUND(42003, "邮箱已绑定其他账号"),
    VERIFICATION_CODE_INVALID(42004, "验证码错误或已过期"),
    VERIFICATION_CODE_TOO_FREQUENT(42005, "验证码发送过于频繁，请稍后再试"),
    IDENTITY_NOT_BOUND(42006, "该身份尚未绑定"),
    LAST_IDENTITY_CANNOT_UNBIND(42007, "至少需保留一种已验证的手机号或邮箱"),
    UNSUPPORTED_ACCOUNT_TYPE(42008, "不支持的账号类型"),
    ACCOUNT_NOT_FOUND(42009, "账号不存在"),
    PASSWORD_SAME_AS_OLD(42010, "新密码不能与原密码相同"),
    INVALID_IDENTITY_FORMAT(42011, "手机号或邮箱格式不正确"),
    PROFILE_UPDATE_INVALID(42012, "资料更新内容不合法"),
    ;

    private final Integer code;
    private final String message;

    UserErrorCode(Integer code, String message) {
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
