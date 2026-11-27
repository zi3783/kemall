package com.kemall.iam.tenancy.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 租户子域通用业务异常（状态不可用、越权、冲突、参数非法）。
 */
public class TenancyBusinessException extends TenancyDomainException {

    private static final long serialVersionUID = 1L;

    public TenancyBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TenancyBusinessException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }
}
