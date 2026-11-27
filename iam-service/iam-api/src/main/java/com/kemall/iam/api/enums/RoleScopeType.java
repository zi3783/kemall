package com.kemall.iam.api.enums;

/**
 * 角色作用域，对应 sys_role.scope_type。
 */
public enum RoleScopeType {

    /** 平台级：tenant_id = 0 且 scope_type = 0 */
    PLATFORM(0),

    /** 租户级 */
    TENANT(1),

    /** 店铺级 */
    SHOP(2),
    ;

    private final int code;

    RoleScopeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RoleScopeType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleScopeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
