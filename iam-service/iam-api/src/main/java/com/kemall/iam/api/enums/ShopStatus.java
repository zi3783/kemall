package com.kemall.iam.api.enums;

/**
 * 店铺状态，对应 shop.status。
 */
public enum ShopStatus {

    /** 正常 */
    ACTIVE(1),

    /** 停用 */
    DISABLED(2),
    ;

    private final int code;

    ShopStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ShopStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ShopStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
