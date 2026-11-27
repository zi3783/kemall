package com.kemall.iam.api.enums;

/**
 * 租户状态，对应 tenant.status。
 */
public enum TenantStatus {

    /** 正常 */
    ACTIVE(1),

    /** 冻结 */
    FROZEN(2),

    /** 关闭（终态） */
    CLOSED(3),
    ;

    private final int code;

    TenantStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TenantStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TenantStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
