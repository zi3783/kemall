package com.kemall.iam.auth.domain.exception;

/**
 * 刷新请求正被并发处理，获取会话锁失败。
 */
public class RefreshBusyException extends AuthDomainException {

    public RefreshBusyException() {
        super(AuthErrorCode.REFRESH_BUSY);
    }
}
