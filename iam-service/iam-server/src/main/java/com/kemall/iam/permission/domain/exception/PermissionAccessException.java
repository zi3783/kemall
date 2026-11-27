package com.kemall.iam.permission.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 角色/成员在当前工作空间下不可操作。
 */
public class PermissionAccessException extends PermissionDomainException {

    private static final long serialVersionUID = 1L;

    public PermissionAccessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PermissionAccessException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }
}
