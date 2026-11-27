package com.kemall.iam.permission.domain.exception;

import java.util.List;
import java.util.Map;

/**
 * 权限编码不存在。
 */
public class PermissionNotFoundException extends PermissionDomainException {

    private static final long serialVersionUID = 1L;

    public PermissionNotFoundException(List<String> permCodes) {
        super(PermissionErrorCode.PERMISSION_NOT_FOUND, Map.of("permCodes", permCodes.toString()));
    }
}
