package com.kemall.iam.user.domain.exception;

import com.kemall.iam.shared.domain.DomainException;
import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 用户子域异常基类。
 * <p>
 * 统一异常锚点，便于全局异常处理器按子域拦截与日志分级。
 */
public abstract class UserDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected UserDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected UserDomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected UserDomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    @Override
    public boolean isExpected() {
        return true;
    }
}
