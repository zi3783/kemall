package com.kemall.iam.user.domain.exception;

/**
 * 用户名已存在。
 */
public class UsernameAlreadyExistsException extends UserDomainException {

    public UsernameAlreadyExistsException() {
        super(UserErrorCode.USERNAME_EXISTS);
    }
}
