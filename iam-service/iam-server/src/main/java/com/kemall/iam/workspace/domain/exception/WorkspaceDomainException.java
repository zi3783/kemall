package com.kemall.iam.workspace.domain.exception;

import com.kemall.iam.shared.domain.DomainException;
import com.kemall.iam.shared.domain.ErrorCode;

import java.util.Map;

/**
 * 工作空间子域异常基类。
 */
public abstract class WorkspaceDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    protected WorkspaceDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected WorkspaceDomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    @Override
    public boolean isExpected() {
        return true;
    }
}
