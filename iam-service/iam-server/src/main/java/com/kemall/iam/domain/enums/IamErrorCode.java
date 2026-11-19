package com.kemall.iam.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IamErrorCode {

    USER_NOT_FOUND("IAM_USER_001", "用户不存在"),

    PASSWORD_ERROR("IAM_AUTH_001", "用户名或密码错误"),

    ACCOUNT_DISABLED("IAM_AUTH_002", "账号已被禁用"),

    TOKEN_INVALID("IAM_AUTH_003", "登录凭证无效"),

    TOKEN_EXPIRED("IAM_AUTH_004", "登录凭证已过期"),

    WORKSPACE_NOT_FOUND("IAM_WORKSPACE_001", "工作空间不存在"),

    WORKSPACE_FORBIDDEN("IAM_WORKSPACE_002", "无权访问该工作空间"),

    PERMISSION_DENIED("IAM_PERMISSION_001", "没有操作权限");

    private final String code;
    private final String message;
}