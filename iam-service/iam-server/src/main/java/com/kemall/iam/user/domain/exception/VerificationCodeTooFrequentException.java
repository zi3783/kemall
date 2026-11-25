package com.kemall.iam.user.domain.exception;

/**
 * 验证码发送过于频繁。
 */
public class VerificationCodeTooFrequentException extends UserDomainException {

    public VerificationCodeTooFrequentException() {
        super(UserErrorCode.VERIFICATION_CODE_TOO_FREQUENT);
    }
}
