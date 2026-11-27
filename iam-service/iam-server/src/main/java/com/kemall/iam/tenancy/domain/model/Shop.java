package com.kemall.iam.tenancy.domain.model;

import com.kemall.iam.api.enums.ShopStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 店铺聚合根（shop）。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    private Long id;

    private Long tenantId;

    private String shopCode;

    private String shopName;

    private String logoUrl;

    /** 1 active 2 disabled */
    private Integer status;

    private String timezone;

    private LocalDateTime createdAt;

    public boolean isActive() {
        return status != null && status == ShopStatus.ACTIVE.getCode();
    }

    public boolean belongsTo(Long tenantId) {
        return this.tenantId != null && this.tenantId.equals(tenantId);
    }

    /**
     * 状态机：ACTIVE ↔ DISABLED。
     */
    public boolean canTransitTo(Integer targetCode) {
        ShopStatus current = ShopStatus.fromCode(status);
        ShopStatus target = ShopStatus.fromCode(targetCode);
        if (current == null || target == null || current == target) {
            return false;
        }
        return true;
    }
}
