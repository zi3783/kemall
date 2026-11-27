package com.kemall.iam.platform.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 平台管理子域通用业务异常。
 */
public class PlatformBusinessException extends PlatformDomainException {

    private static final long serialVersionUID = 1L;

    public PlatformBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PlatformBusinessException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }
}
