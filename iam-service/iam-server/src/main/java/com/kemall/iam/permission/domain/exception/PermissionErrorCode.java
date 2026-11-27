package com.kemall.iam.permission.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 权限子域错误码。
 */
public enum PermissionErrorCode implements ErrorCode {

    PERMISSION_REQUEST_INVALID(40021, "权限操作参数不合法"),

    ROLE_ACCESS_DENIED(40320, "无权操作该角色"),

    ROLE_SCOPE_MISMATCH(40321, "角色作用域与当前工作空间不匹配"),

    TENANT_MEMBER_UNAVAILABLE(40322, "租户成员不存在或已停用"),

    PLATFORM_MEMBER_UNAVAILABLE(40323, "平台成员不存在或已停用"),

    ROLE_NOT_FOUND(40420, "角色不存在"),

    PERMISSION_NOT_FOUND(40421, "权限不存在"),
    ;

    private final Integer code;
    private final String message;

    PermissionErrorCode(Integer code, String message) {
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
