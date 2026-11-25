package com.kemall.iam.auth.domain.exception;

/**
 * Refresh Token 重放：轮换后旧 Refresh 再次被使用。
 * 处置：撤销整个会话并强制所有设备重新登录。
 */
public class RefreshTokenReusedException extends AuthDomainException {

    public RefreshTokenReusedException() {
        super(AuthErrorCode.REFRESH_TOKEN_REUSED);
    }
}
