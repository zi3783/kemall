package com.kemall.iam.permission.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 角色领域对象（sys_role）。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    private Long id;

    private Long tenantId;

    private String roleCode;

    private String roleName;

    /** 0 platform 1 tenant 2 shop */
    private Integer scopeType;

    private Integer status;

    public boolean isEnabled() {
        return status != null && status == 1;
    }

    public boolean isPlatformRole() {
        return scopeType != null && scopeType == 0;
    }

    public boolean matches(Long expectedTenantId, int expectedScopeType) {
        return tenantId != null
                && tenantId.equals(expectedTenantId)
                && scopeType != null
                && scopeType == expectedScopeType;
    }
}
