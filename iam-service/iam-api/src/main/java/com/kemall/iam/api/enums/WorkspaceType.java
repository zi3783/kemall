package com.kemall.iam.api.enums;

/**
 * 工作空间类型。
 */
public enum WorkspaceType {

    /** 未选择任何工作空间 */
    NONE,

    /** 租户级（shop_id = 0） */
    TENANT,

    /** 店铺级 */
    SHOP,

    /** 平台管理 */
    PLATFORM
}
