package com.kemall.iam.customer.domain.exception;

import com.kemall.iam.shared.domain.DomainException;
import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 消费者子域异常基类。
 */
public abstract class CustomerDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected CustomerDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected CustomerDomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    @Override
    public boolean isExpected() {
        return true;
    }
}
