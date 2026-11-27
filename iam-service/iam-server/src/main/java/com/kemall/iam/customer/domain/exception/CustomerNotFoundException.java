package com.kemall.iam.customer.domain.exception;

import java.util.Map;

/**
 * 消费者身份不存在。
 */
public class CustomerNotFoundException extends CustomerDomainException {

    private static final long serialVersionUID = 1L;

    public CustomerNotFoundException(Long userId) {
        super(CustomerErrorCode.CUSTOMER_NOT_FOUND, Map.of("userId", String.valueOf(userId)));
    }
}
