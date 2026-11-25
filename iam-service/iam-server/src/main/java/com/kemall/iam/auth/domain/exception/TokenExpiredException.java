package com.kemall.iam.auth.domain.exception;

/**
 * Token 过期异常。
 */
public class TokenExpiredException extends AuthDomainException {

    public TokenExpiredException() {
        super(AuthErrorCode.TOKEN_EXPIRED);
    }
}
