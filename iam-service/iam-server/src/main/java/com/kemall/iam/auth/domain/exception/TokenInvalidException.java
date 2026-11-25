package com.kemall.iam.auth.domain.exception;

/**
 * Token 非法（签名不匹配、结构错误、jti 重放等）。
 */
public class TokenInvalidException extends AuthDomainException {

    public TokenInvalidException() {
        super(AuthErrorCode.TOKEN_INVALID);
    }

    public TokenInvalidException(String detail) {
        super(AuthErrorCode.TOKEN_INVALID,
                detail == null ? java.util.Collections.emptyMap()
                        : java.util.Map.of("detail", detail));
    }
}
