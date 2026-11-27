package com.kemall.iam.tenancy.domain.exception;

import java.util.Map;

/**
 * 租户不存在。
 */
public class TenantNotFoundException extends TenancyDomainException {

    private static final long serialVersionUID = 1L;

    public TenantNotFoundException(Long tenantId) {
        super(TenancyErrorCode.TENANT_NOT_FOUND, Map.of("tenantId", String.valueOf(tenantId)));
    }
}
