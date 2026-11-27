package com.kemall.iam.permission.domain.exception;

import java.util.Map;

/**
 * 角色不存在。
 */
public class RoleNotFoundException extends PermissionDomainException {

    private static final long serialVersionUID = 1L;

    public RoleNotFoundException(Long roleId) {
        super(PermissionErrorCode.ROLE_NOT_FOUND, Map.of("roleId", String.valueOf(roleId)));
    }
}
