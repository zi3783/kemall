package com.kemall.iam.user.domain.exception;

/**
 * 不支持的账号类型（如用用户名做密码重置）。
 */
public class UnsupportedAccountTypeException extends UserDomainException {

    public UnsupportedAccountTypeException() {
        super(UserErrorCode.UNSUPPORTED_ACCOUNT_TYPE);
    }
}
