package com.kemall.iam.auth.domain.exception;

import java.util.Collections;
import java.util.Map;

/**
 * 认证子域异常基类
 * <p>
 * 所有认证子域（登录、Token、密码、MFA 等）的具体异常都应继承此类。
 * 继承层级：
 * <pre>
 * RuntimeException
 *  └── DomainException
 *       └── AuthDomainException
 *            ├── LoginFailedException
 *            ├── AccountNotLoginAllowedException
 *            ├── TokenExpiredException
 *            └── ...
 * </pre>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>为认证子域提供统一异常锚点，便于全局异常处理器按子域拦截</li>
 *   <li>子域内统一处理审计/风控（如登录失败计数、账号冻结）</li>
 *   <li>便于后续扩展认证专属能力（如携带 traceId、客户端 IP 等）</li>
 * </ul>
 */
public abstract class AuthDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    /**
     * 认证子域统一前缀（可选，用于日志检索/监控告警区分）
     * 例如：AUTH_40001
     */
    private static final String LOG_PREFIX = "AUTH";

    protected AuthDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected AuthDomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected AuthDomainException(ErrorCode errorCode, Map<String, Object> context) {
        super(errorCode, context);
    }

    protected AuthDomainException(ErrorCode errorCode, Map<String, Object> context, Throwable cause) {
        super(errorCode, context, cause);
    }

    /**
     * 统一的日志/监控标识，格式：AUTH_{code}
     * <p>
     * 便于在 ELK / Prometheus / Sentry 中按子域过滤与告警。
     */
    public String getLogTag() {
        return LOG_PREFIX + "_" + getCode();
    }

    /**
     * 是否属于"可预期"的业务异常（用于日志分级）
     * <p>
     * 认证子域异常默认为可预期，日志用 warn 级别；
     * 若某个子类需要 error 级别，可覆盖此方法返回 false。
     */
    public boolean isExpected() {
        return true;
    }
}