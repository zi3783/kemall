package com.kemall.iam.customer.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 消费者子域错误码。
 */
public enum CustomerErrorCode implements ErrorCode {

    CUSTOMER_UNAVAILABLE(40350, "消费者身份已禁用"),

    CUSTOMER_NOT_FOUND(40450, "消费者身份不存在"),

    CUSTOMER_ALREADY_EXISTS(40950, "该账号已具备消费者身份"),
    ;

    private final Integer code;
    private final String message;

    CustomerErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
