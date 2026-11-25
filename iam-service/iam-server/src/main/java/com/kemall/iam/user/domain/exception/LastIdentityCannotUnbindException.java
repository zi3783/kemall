package com.kemall.iam.user.domain.exception;

/**
 * 解绑后无任何已验证身份，拒绝操作。
 */
public class LastIdentityCannotUnbindException extends UserDomainException {

    public LastIdentityCannotUnbindException() {
        super(UserErrorCode.LAST_IDENTITY_CANNOT_UNBIND);
    }
}
