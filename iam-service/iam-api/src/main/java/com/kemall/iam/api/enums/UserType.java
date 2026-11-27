package com.kemall.iam.api.enums;

/**
 * 登录账号的身份类型。
 * <p>
 * 同一全局账号可兼具多种身份：消费者、租户/店铺成员、平台管理员。
 */
public enum UserType {

    /** 消费者（customer） */
    CUSTOMER,

    /** 商家后台成员（tenant_member） */
    TENANT_MEMBER,

    /** 平台管理员（platform_member） */
    PLATFORM_MEMBER,
}
