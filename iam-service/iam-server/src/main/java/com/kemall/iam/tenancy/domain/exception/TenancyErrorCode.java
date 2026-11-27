package com.kemall.iam.tenancy.domain.exception;

import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 租户子域错误码。
 */
public enum TenancyErrorCode implements ErrorCode {

    TENANCY_REQUEST_INVALID(40030, "租户/店铺/成员操作参数不合法"),

    TENANT_UNAVAILABLE(40330, "租户已冻结、关闭或过期"),

    SHOP_UNAVAILABLE(40331, "店铺已停用"),

    SHOP_NOT_IN_TENANT(40332, "店铺不属于该租户"),

    MEMBER_UNAVAILABLE(40333, "成员已停用或已移除"),

    USER_ACCOUNT_UNAVAILABLE(40334, "用户账号不存在或不可用"),

    TENANCY_ACCESS_DENIED(40335, "无权访问该租户或店铺数据"),

    TENANT_NOT_FOUND(40430, "租户不存在"),

    SHOP_NOT_FOUND(40431, "店铺不存在"),

    MEMBER_NOT_FOUND(40432, "成员不存在"),

    TENANT_CODE_DUPLICATED(40930, "租户编码已存在"),

    SHOP_CODE_DUPLICATED(40931, "该租户下店铺编码已存在"),

    MEMBER_ALREADY_EXISTS(40932, "该用户已是此范围成员"),
    ;

    private final Integer code;
    private final String message;

    TenancyErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
