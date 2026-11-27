package com.kemall.iam.permission.domain.exception;

import com.kemall.iam.shared.domain.DomainException;
import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 权限子域异常基类。
 */
public abstract class PermissionDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected PermissionDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected PermissionDomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    @Override
    public boolean isExpected() {
        return true;
    }
}
