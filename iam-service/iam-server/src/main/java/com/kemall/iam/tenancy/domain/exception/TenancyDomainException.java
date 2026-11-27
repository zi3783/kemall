package com.kemall.iam.tenancy.domain.exception;

import com.kemall.iam.shared.domain.DomainException;
import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 租户子域异常基类。
 */
public abstract class TenancyDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected TenancyDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected TenancyDomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    @Override
    public boolean isExpected() {
        return true;
    }
}
