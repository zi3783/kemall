package com.kemall.iam.auth.infrastructure.persistence.repository;

import com.kemall.iam.api.dto.auth.WorkspaceInfo;
import com.kemall.iam.auth.domain.repository.WorkspaceRepository;
import com.kemall.iam.auth.infrastructure.persistence.mapper.WorkspaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WorkspaceRepositoryImpl implements WorkspaceRepository {

    private final WorkspaceMapper workspaceMapper;

    @Override
    public List<WorkspaceInfo> findAvailableWorkspaces(Long userId) {
        return workspaceMapper.selectAvailableWorkspaces(userId);
    }
}