package com.kemall.iam.permission.domain.model;

import com.kemall.iam.api.enums.RoleScopeType;
import com.kemall.iam.api.enums.WorkspaceType;

/**
 * 权限判定作用域（来自当前会话的工作空间上下文）。
 */
public record PermissionScope(
        WorkspaceType workspaceType,
        Long tenantId,
        Long memberId,
        Long platformMemberId) {

    public boolean isPlatform() {
        return WorkspaceType.PLATFORM == workspaceType;
    }

    public boolean isTenantOrShop() {
        return WorkspaceType.TENANT == workspaceType || WorkspaceType.SHOP == workspaceType;
    }

    public boolean isEmpty() {
        return workspaceType == null || WorkspaceType.NONE == workspaceType;
    }

    /**
     * 当前工作空间可操作/可见的角色作用域。
     */
    public RoleScopeType roleScopeType() {
        if (workspaceType == null) {
            return null;
        }
        return switch (workspaceType) {
            case PLATFORM -> RoleScopeType.PLATFORM;
            case TENANT -> RoleScopeType.TENANT;
            case SHOP -> RoleScopeType.SHOP;
            default -> null;
        };
    }

    /**
     * 角色归属的租户 ID：平台角色固定为 0。
     */
    public Long roleTenantId() {
        return isPlatform() ? 0L : tenantId;
    }
}
