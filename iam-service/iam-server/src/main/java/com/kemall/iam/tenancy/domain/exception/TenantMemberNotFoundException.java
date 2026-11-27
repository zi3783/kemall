package com.kemall.iam.tenancy.domain.exception;

import java.util.Map;

/**
 * 成员不存在。
 */
public class TenantMemberNotFoundException extends TenancyDomainException {

    private static final long serialVersionUID = 1L;

    public TenantMemberNotFoundException(Long memberId) {
        super(TenancyErrorCode.MEMBER_NOT_FOUND, Map.of("memberId", String.valueOf(memberId)));
    }
}
