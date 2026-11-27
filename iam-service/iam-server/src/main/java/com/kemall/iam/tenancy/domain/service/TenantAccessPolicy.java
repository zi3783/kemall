package com.kemall.iam.tenancy.domain.service;

import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.tenancy.domain.exception.TenancyBusinessException;
import com.kemall.iam.tenancy.domain.exception.TenancyErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 租户数据访问策略：根据当前工作空间限定可操作的租户范围。
 */
@Component
public class TenantAccessPolicy {

    public boolean isPlatform(LoginPrincipal principal) {
        return principal.getPlatformMemberId() != null;
    }

    public boolean isTenantWorkspace(LoginPrincipal principal) {
        return !isPlatform(principal)
                && principal.getTenantId() != null
                && (principal.getShopId() == null || principal.getShopId() == 0L);
    }

    public boolean isShopWorkspace(LoginPrincipal principal) {
        return !isPlatform(principal)
                && principal.getShopId() != null
                && principal.getShopId() > 0L;
    }

    public void requirePlatform(LoginPrincipal principal) {
        if (!isPlatform(principal)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
    }

    /**
     * 解析本次操作的租户 ID：
     * 平台必须显式传参；租户/店铺空间强制取会话租户且不允许篡改。
     */
    public Long resolveTenantId(LoginPrincipal principal, Long requestedTenantId) {
        if (isPlatform(principal)) {
            if (requestedTenantId == null) {
                throw new TenancyBusinessException(TenancyErrorCode.TENANCY_REQUEST_INVALID,
                        Map.of("field", "tenantId"));
            }
            return requestedTenantId;
        }
        Long sessionTenantId = principal.getTenantId();
        if (sessionTenantId == null) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
        if (requestedTenantId != null && !requestedTenantId.equals(sessionTenantId)) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
        return sessionTenantId;
    }

    /**
     * 读取校验：平台可读任意租户，其他空间只能读自己的租户。
     */
    public void requireTenantReadable(LoginPrincipal principal, Long tenantId) {
        if (isPlatform(principal)) {
            return;
        }
        if (tenantId == null || !tenantId.equals(principal.getTenantId())) {
            throw new TenancyBusinessException(TenancyErrorCode.TENANCY_ACCESS_DENIED);
        }
    }
}
