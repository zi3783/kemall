package com.kemall.iam.workspace.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 成员关系存在，但租户/店铺/平台侧状态不允许进入。
 */
public class WorkspaceUnavailableException extends WorkspaceDomainException {

    private static final long serialVersionUID = 1L;

    public WorkspaceUnavailableException(ErrorCode errorCode) {
        super(errorCode);
    }
}
