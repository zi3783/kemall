package com.kemall.iam.workspace.domain.repository;

import com.kemall.iam.workspace.domain.model.Workspace;

import java.util.List;
import java.util.Optional;

/**
 * 工作空间仓储端口（workspace 子域）。
 * <p>
 * 返回带成员/租户/店铺/平台成员实时状态的领域对象，
 * 状态判定交给领域服务，仓储不做过早过滤。
 */
public interface WorkspaceRepository {

    /**
     * 用户当前可进入的工作空间列表（已过滤失效租户/店铺/成员）。
     */
    List<Workspace> findAvailableWorkspaces(Long userId);

    /**
     * 用户在某租户下的租户级成员关系（shop_id = 0），不校验状态。
     */
    Optional<Workspace> findTenantWorkspace(Long userId, Long tenantId);

    /**
     * 用户在某店铺下的成员关系，不校验状态。
     */
    Optional<Workspace> findShopWorkspace(Long userId, Long tenantId, Long shopId);

    /**
     * 用户的平台成员关系（含合规平台角色数量），不校验状态。
     */
    Optional<Workspace> findPlatformWorkspace(Long userId);
}
