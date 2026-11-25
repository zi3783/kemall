package com.kemall.iam.auth.domain.exception;

/**
 * 用户不存在。
 */
public class UserNotFoundException extends AuthDomainException {

    public UserNotFoundException() {
        super(AuthErrorCode.USER_NOT_FOUND);
    }
}
