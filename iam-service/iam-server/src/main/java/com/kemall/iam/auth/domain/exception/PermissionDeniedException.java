package com.kemall.iam.auth.domain.exception;

/**
 * 无权限访问。预留给后续 @PreAuthorize。
 */
public class PermissionDeniedException extends AuthDomainException {

    public PermissionDeniedException() {
        super(AuthErrorCode.PERMISSION_DENIED);
    }
}
