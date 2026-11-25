package com.kemall.iam.user.domain.exception;

/**
 * 资料更新请求内容不合法（空更新 / 昵称空白等）。
 */
public class ProfileUpdateInvalidException extends UserDomainException {

    public ProfileUpdateInvalidException() {
        super(UserErrorCode.PROFILE_UPDATE_INVALID);
    }
}
