package com.kemall.iam.auth.domain.exception;

/**
 * 新密码与旧密码相同。
 */
public class PasswordSameAsOldException extends AuthDomainException {

    public PasswordSameAsOldException() {
        super(AuthErrorCode.PASSWORD_SAME_AS_OLD);
    }
}
