package com.kemall.iam.tenancy.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 租户/店铺成员聚合根（tenant_member）。
 * <p>
 * shopId = 0 表示租户级成员；大于 0 表示店铺成员。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMember {

    private Long id;

    private Long tenantId;

    private Long shopId;

    private Long userId;

    /** 1 buyer 2 staff 3 tenant admin */
    private Integer memberType;

    /** 1 active 2 disabled */
    private Integer status;

    private LocalDateTime joinedAt;

    /** 非空表示已移除 */
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return status != null && status == 1 && deletedAt == null;
    }

    public boolean isRemoved() {
        return deletedAt != null;
    }

    public boolean isTenantLevel() {
        return shopId == null || shopId == 0L;
    }

    public boolean belongsToTenant(Long tenantId) {
        return this.tenantId != null && this.tenantId.equals(tenantId);
    }

    public boolean belongsToShop(Long tenantId, Long shopId) {
        return belongsToTenant(tenantId) && this.shopId != null && this.shopId.equals(shopId);
    }
}
