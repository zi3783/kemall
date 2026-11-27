package com.kemall.iam.customer.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 消费者子域通用业务异常。
 */
public class CustomerBusinessException extends CustomerDomainException {

    private static final long serialVersionUID = 1L;

    public CustomerBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CustomerBusinessException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }
}
