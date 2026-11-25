package com.kemall.iam.auth.domain.exception;

/**
 * 会话不存在、已撤销、已过期或归属不匹配。
 */
public class SessionInvalidException extends AuthDomainException {

    public SessionInvalidException() {
        super(AuthErrorCode.SESSION_INVALID);
    }
}
