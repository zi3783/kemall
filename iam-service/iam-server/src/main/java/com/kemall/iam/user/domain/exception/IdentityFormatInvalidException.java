package com.kemall.iam.user.domain.exception;

/**
 * 手机号/邮箱格式不合法。
 */
public class IdentityFormatInvalidException extends UserDomainException {

    public IdentityFormatInvalidException() {
        super(UserErrorCode.INVALID_IDENTITY_FORMAT);
    }
}
