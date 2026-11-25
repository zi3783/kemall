package com.kemall.iam.user.domain.exception;

/**
 * 该身份未绑定。
 */
public class IdentityNotBoundException extends UserDomainException {

    public IdentityNotBoundException() {
        super(UserErrorCode.IDENTITY_NOT_BOUND);
    }
}
