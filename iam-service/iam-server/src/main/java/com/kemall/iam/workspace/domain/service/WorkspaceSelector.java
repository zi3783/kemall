package com.kemall.iam.workspace.domain.service;

import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.workspace.domain.exception.WorkspaceAccessDeniedException;
import com.kemall.iam.workspace.domain.exception.WorkspaceErrorCode;
import com.kemall.iam.workspace.domain.exception.WorkspaceSelectionInvalidException;
import com.kemall.iam.workspace.domain.exception.WorkspaceUnavailableException;
import com.kemall.iam.workspace.domain.model.Workspace;
import com.kemall.iam.workspace.domain.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 工作空间准入领域服务。
 * <p>
 * 三类工作空间的选择规则集中在此：
 * <ul>
 *   <li>租户级：tenant_member(shop_id=0) 有效 + 租户可用</li>
 *   <li>店铺级：tenant_member(shop_id=店铺) 有效 + 租户可用 + 店铺启用</li>
 *   <li>平台：platform_member 启用 + 至少一个合规平台角色</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class WorkspaceSelector {

    private final WorkspaceRepository workspaceRepository;

    /**
     * 租户级工作空间选择。
     */
    public Workspace selectTenant(Long userId, Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new WorkspaceSelectionInvalidException();
        }
        Workspace workspace = workspaceRepository.findTenantWorkspace(userId, tenantId)
                .orElseThrow(() -> new WorkspaceAccessDeniedException(WorkspaceType.TENANT, tenantId, 0L));
        ensureTenantAvailable(workspace);
        ensureMemberAvailable(workspace);
        return workspace;
    }

    /**
     * 店铺级工作空间选择。
     */
    public Workspace selectShop(Long userId, Long tenantId, Long shopId) {
        if (tenantId == null || tenantId <= 0 || shopId == null || shopId <= 0) {
            throw new WorkspaceSelectionInvalidException();
        }
        Workspace workspace = workspaceRepository.findShopWorkspace(userId, tenantId, shopId)
                .orElseThrow(() -> new WorkspaceAccessDeniedException(WorkspaceType.SHOP, tenantId, shopId));
        ensureTenantAvailable(workspace);
        ensureShopAvailable(workspace);
        ensureMemberAvailable(workspace);
        return workspace;
    }

    /**
     * 平台管理工作空间选择。
     * <p>
     * 除 platform_member 启用外，还要求至少一个 role 满足
     * tenant_id = 0 且 scope_type = 0（对应建表脚本末尾的平台角色约定）。
     */
    public Workspace selectPlatform(Long userId) {
        Workspace workspace = workspaceRepository.findPlatformWorkspace(userId)
                .orElseThrow(() -> new WorkspaceAccessDeniedException(WorkspaceType.PLATFORM, 0L, 0L));
        ensurePlatformAvailable(workspace);
        return workspace;
    }

    /**
     * 按当前会话上下文解析有效工作空间，失效时返回空。
     */
    public Optional<Workspace> resolveCurrent(Long userId,
                                              Long tenantId,
                                              Long shopId,
                                              Long platformMemberId) {
        Optional<Workspace> ws;
        if (platformMemberId != null) {
            ws = workspaceRepository.findPlatformWorkspace(userId)
                    .filter(workspace -> platformMemberId.equals(workspace.getPlatformMemberId()))
                    .filter(this::isPlatformAvailable);
            ws.ifPresent(workspace -> workspace.setType(WorkspaceType.PLATFORM));
            return ws;
        }
        if (shopId != null && shopId > 0) {
            ws = workspaceRepository.findShopWorkspace(userId, tenantId, shopId)
                    .filter(this::isTenantAvailable)
                    .filter(this::isShopAvailable)
                    .filter(this::isMemberAvailable);
            ws.ifPresent(workspace -> workspace.setType(WorkspaceType.SHOP));
            return ws;
        }
        if (tenantId != null) {
            ws = workspaceRepository.findTenantWorkspace(userId, tenantId)
                    .filter(this::isTenantAvailable)
                    .filter(this::isMemberAvailable);
            ws.ifPresent(workspace -> workspace.setType(WorkspaceType.TENANT));
            return ws;
        }
        return Optional.empty();
    }

    // ==================== 状态判定 ====================

    private void ensureTenantAvailable(Workspace workspace) {
        if (!isTenantAvailable(workspace)) {
            throw new WorkspaceUnavailableException(WorkspaceErrorCode.WORKSPACE_TENANT_UNAVAILABLE);
        }
    }

    private void ensureShopAvailable(Workspace workspace) {
        if (!isShopAvailable(workspace)) {
            throw new WorkspaceUnavailableException(WorkspaceErrorCode.WORKSPACE_SHOP_UNAVAILABLE);
        }
    }

    private void ensureMemberAvailable(Workspace workspace) {
        if (!isMemberAvailable(workspace)) {
            throw new WorkspaceUnavailableException(WorkspaceErrorCode.WORKSPACE_MEMBER_DISABLED);
        }
    }

    private void ensurePlatformAvailable(Workspace workspace) {
        if (!isPlatformAvailable(workspace)) {
            throw new WorkspaceUnavailableException(WorkspaceErrorCode.WORKSPACE_PLATFORM_UNAVAILABLE);
        }
    }

    private boolean isTenantAvailable(Workspace workspace) {
        if (workspace.getTenantStatus() == null || workspace.getTenantStatus() != 1) {
            return false;
        }
        return workspace.getTenantExpiresAt() == null
                || workspace.getTenantExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean isShopAvailable(Workspace workspace) {
        return workspace.getShopStatus() != null && workspace.getShopStatus() == 1;
    }

    private boolean isMemberAvailable(Workspace workspace) {
        return workspace.getMemberDeletedAt() == null
                && workspace.getMemberStatus() != null
                && workspace.getMemberStatus() == 1;
    }

    private boolean isPlatformAvailable(Workspace workspace) {
        return workspace.getPlatformMemberStatus() != null
                && workspace.getPlatformMemberStatus() == 1
                && workspace.getPlatformRoleCount() != null
                && workspace.getPlatformRoleCount() > 0;
    }
}
