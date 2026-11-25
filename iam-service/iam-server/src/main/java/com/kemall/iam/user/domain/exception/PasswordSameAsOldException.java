package com.kemall.iam.user.domain.exception;

/**
 * 新密码与原密码相同。
 */
public class PasswordSameAsOldException extends UserDomainException {

    public PasswordSameAsOldException() {
        super(UserErrorCode.PASSWORD_SAME_AS_OLD);
    }
}
