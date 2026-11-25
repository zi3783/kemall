package com.kemall.iam.user.domain.exception;

/**
 * 账号不存在（模糊提示，用于防账号枚举）。
 */
public class AccountNotFoundException extends UserDomainException {

    public AccountNotFoundException() {
        super(UserErrorCode.ACCOUNT_NOT_FOUND);
    }
}
