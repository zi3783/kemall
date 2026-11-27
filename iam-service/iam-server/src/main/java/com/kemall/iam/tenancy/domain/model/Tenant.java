package com.kemall.iam.tenancy.domain.model;

import com.kemall.iam.api.enums.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 租户聚合根（tenant）。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    private Long id;

    private String tenantCode;

    private String tenantName;

    private String planCode;

    /** 1 active 2 frozen 3 closed */
    private Integer status;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    public boolean isActive() {
        return status != null && status == TenantStatus.ACTIVE.getCode();
    }

    /**
     * 是否可用于新业务（状态正常且未过期）。
     */
    public boolean isUsable(LocalDateTime now) {
        return isActive() && (expiresAt == null || expiresAt.isAfter(now));
    }

    /**
     * 状态机：CLOSED 为终态，其余允许互转。
     */
    public boolean canTransitTo(Integer targetCode) {
        TenantStatus current = TenantStatus.fromCode(status);
        TenantStatus target = TenantStatus.fromCode(targetCode);
        if (current == null || target == null || current == target) {
            return false;
        }
        return current != TenantStatus.CLOSED;
    }
}
