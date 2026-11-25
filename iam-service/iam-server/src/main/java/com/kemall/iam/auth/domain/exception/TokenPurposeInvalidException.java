package com.kemall.iam.auth.domain.exception;

/**
 * Token 用途（ACCESS / REFRESH）不匹配。
 */
public class TokenPurposeInvalidException extends AuthDomainException {

    public TokenPurposeInvalidException() {
        super(AuthErrorCode.TOKEN_PURPOSE_INVALID);
    }
}
