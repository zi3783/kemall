package com.kemall.iam.api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberType {

    BUYER(1, "买家"),
    STAFF(2, "员工"),
    TENANT_ADMIN(3, "租户管理员");

    private final Integer code;
    private final String description;
}