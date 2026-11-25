package com.kemall.iam.user.domain.exception;

import com.kemall.iam.api.enums.IdentityType;

/**
 * 手机号/邮箱已被其他账号绑定。
 */
public class IdentityAlreadyBoundException extends UserDomainException {

    public IdentityAlreadyBoundException(IdentityType identityType) {
        super(identityType == IdentityType.EMAIL
                ? UserErrorCode.EMAIL_ALREADY_BOUND
                : UserErrorCode.MOBILE_ALREADY_BOUND);
    }
}
