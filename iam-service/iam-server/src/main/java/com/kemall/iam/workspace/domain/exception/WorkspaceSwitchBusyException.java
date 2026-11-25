package com.kemall.iam.workspace.domain.exception;

/**
 * 同一会话正在执行刷新或切换。
 */
public class WorkspaceSwitchBusyException extends WorkspaceDomainException {

    private static final long serialVersionUID = 1L;

    public WorkspaceSwitchBusyException() {
        super(WorkspaceErrorCode.WORKSPACE_SWITCH_BUSY);
    }
}
