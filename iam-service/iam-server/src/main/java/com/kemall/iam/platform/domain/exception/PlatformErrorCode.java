package com.kemall.iam.platform.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 平台管理子域错误码。
 */
public enum PlatformErrorCode implements ErrorCode {

    PLATFORM_REQUEST_INVALID(40040, "平台管理操作参数不合法"),

    PLATFORM_ACCESS_DENIED(40340, "无权访问平台管理数据"),

    PLATFORM_MEMBER_UNAVAILABLE(40341, "平台成员已禁用"),

    PLATFORM_USER_UNAVAILABLE(40342, "用户账号不存在或不可用"),

    PLATFORM_MEMBER_NOT_FOUND(40440, "平台成员不存在"),

    PLATFORM_MEMBER_ALREADY_EXISTS(40940, "该用户已是平台成员"),
    ;

    private final Integer code;
    private final String message;

    PlatformErrorCode(Integer code, String message) {
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
