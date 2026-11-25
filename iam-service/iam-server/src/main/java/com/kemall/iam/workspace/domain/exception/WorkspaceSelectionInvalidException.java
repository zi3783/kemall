package com.kemall.iam.workspace.domain.exception;

/**
 * 工作空间选择参数与类型不匹配。
 */
public class WorkspaceSelectionInvalidException extends WorkspaceDomainException {

    private static final long serialVersionUID = 1L;

    public WorkspaceSelectionInvalidException() {
        super(WorkspaceErrorCode.WORKSPACE_SELECTION_INVALID);
    }
}
