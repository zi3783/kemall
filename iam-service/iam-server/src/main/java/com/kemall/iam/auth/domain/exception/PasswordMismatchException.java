package com.kemall.iam.auth.domain.exception;

/**
 * 原密码不匹配。
 */
public class PasswordMismatchException extends AuthDomainException {

    public PasswordMismatchException() {
        super(AuthErrorCode.PASSWORD_MISMATCH);
    }
}
