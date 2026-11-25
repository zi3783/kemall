package com.kemall.iam.user.domain.exception;

/**
 * 验证码错误或已过期。
 */
public class VerificationCodeInvalidException extends UserDomainException {

    public VerificationCodeInvalidException() {
        super(UserErrorCode.VERIFICATION_CODE_INVALID);
    }
}
