package com.kemall.iam.workspace.infrastructure.persistence.repository;

import com.kemall.iam.workspace.domain.model.Workspace;
import com.kemall.iam.workspace.domain.repository.WorkspaceRepository;
import com.kemall.iam.workspace.infrastructure.persistence.mapper.WorkspaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工作空间仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class WorkspaceRepositoryImpl implements WorkspaceRepository {

    private final WorkspaceMapper workspaceMapper;

    @Override
    public List<Workspace> findAvailableWorkspaces(Long userId) {
        return workspaceMapper.selectAvailableWorkspaces(userId);
    }

    @Override
    public Optional<Workspace> findTenantWorkspace(Long userId, Long tenantId) {
        return Optional.ofNullable(workspaceMapper.selectTenantWorkspace(userId, tenantId));
    }

    @Override
    public Optional<Workspace> findShopWorkspace(Long userId, Long tenantId, Long shopId) {
        return Optional.ofNullable(workspaceMapper.selectShopWorkspace(userId, tenantId, shopId));
    }

    @Override
    public Optional<Workspace> findPlatformWorkspace(Long userId) {
        return Optional.ofNullable(workspaceMapper.selectPlatformWorkspace(userId));
    }
}
