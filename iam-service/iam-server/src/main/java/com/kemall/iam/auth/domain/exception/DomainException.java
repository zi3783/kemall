package com.kemall.iam.auth.domain.exception;

import java.util.Collections;
import java.util.Map;

/**
 * 领域异常根基类
 * <p>
 * 所有领域层异常都必须继承此类，代表"业务规则被违背"，
 * 与技术异常（SQLException、IOException 等）严格区分。
 */
public abstract class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 业务错误码
     */
    private final transient ErrorCode errorCode;

    /**
     * 结构化上下文（用于日志、前端提示、排查）
     * 使用 transient 防止异常序列化时携带大对象
     */
    private final transient Map<String, Object> context;

    protected DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.context = Collections.emptyMap();
    }

    protected DomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.context = Collections.emptyMap();
    }

    protected DomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.context = context == null ? Collections.emptyMap() : context;
    }

    protected DomainException(ErrorCode errorCode, Map<String, Object> context, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.context = context == null ? Collections.emptyMap() : context;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Integer getCode() {
        return errorCode.getCode();
    }

    public Map<String, Object> getContext() {
        return context;
    }
}