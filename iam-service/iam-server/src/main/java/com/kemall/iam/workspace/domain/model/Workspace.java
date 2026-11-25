package com.kemall.iam.workspace.domain.model;

import com.kemall.iam.api.enums.WorkspaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 工作空间聚合快照（领域对象）。
 * <p>
 * 由仓储按"用户 + 空间"查询出成员、租户、店铺、平台成员的实时状态，
 * 是否可进入由 {@link com.kemall.iam.workspace.domain.service.WorkspaceSelector} 判定。
 * <p>
 * 平台工作空间的 tenantId / shopId 固定为 0。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

    private WorkspaceType type;

    private Long tenantId;

    private String tenantName;

    /** 1 active 2 frozen 3 closed */
    private Integer tenantStatus;

    private LocalDateTime tenantExpiresAt;

    private Long shopId;

    private String shopName;

    private Integer shopStatus;

    /** 对应 tenant_member.id */
    private Long memberId;

    /** 1 buyer 2 staff 3 tenant admin */
    private Integer memberType;

    private Integer memberStatus;

    private LocalDateTime memberDeletedAt;

    /** 对应 platform_member.id */
    private Long platformMemberId;

    private Integer platformMemberStatus;

    /** 该平台成员拥有的合规平台角色（tenant_id=0 且 scope_type=0）数量 */
    private Long platformRoleCount;

    public boolean isPlatform() {
        return WorkspaceType.PLATFORM == type;
    }

    public boolean isTenantLevel() {
        return WorkspaceType.TENANT == type;
    }

    public boolean isShopLevel() {
        return WorkspaceType.SHOP == type;
    }
}
