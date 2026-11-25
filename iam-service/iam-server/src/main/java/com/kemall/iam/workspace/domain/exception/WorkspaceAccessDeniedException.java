package com.kemall.iam.workspace.domain.exception;

import com.kemall.iam.api.enums.WorkspaceType;

import java.util.Map;

/**
 * 不具备目标工作空间成员关系。
 */
public class WorkspaceAccessDeniedException extends WorkspaceDomainException {

    private static final long serialVersionUID = 1L;

    public WorkspaceAccessDeniedException(WorkspaceType type, Long tenantId, Long shopId) {
        super(WorkspaceErrorCode.WORKSPACE_ACCESS_DENIED,
                Map.of("workspaceType", type.name(),
                        "tenantId", String.valueOf(tenantId),
                        "shopId", String.valueOf(shopId)));
    }
}
