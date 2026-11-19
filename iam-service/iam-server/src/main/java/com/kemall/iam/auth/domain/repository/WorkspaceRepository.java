package com.kemall.iam.auth.domain.repository;

import com.kemall.iam.api.dto.auth.WorkspaceInfo;

import java.util.List;

public interface WorkspaceRepository {

    /**
     * 查询用户可进入的工作空间
     */
    List<WorkspaceInfo> findAvailableWorkspaces(Long userId);
}