package com.kemall.iam.platform.domain.exception;

import com.kemall.iam.shared.domain.DomainException;
import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 平台管理子域异常基类。
 */
public abstract class PlatformDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected PlatformDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected PlatformDomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    @Override
    public boolean isExpected() {
        return true;
    }
}
