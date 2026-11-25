package com.kemall.iam.workspace.domain.exception;

import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.shared.domain.ErrorCode;

/**
 * 工作空间子域错误码。
 * <p>
 * 码段约定与全局异常处理器保持一致：
 * 400xx 参数类、403xx 权限类、409xx 并发冲突类。
 */
public enum WorkspaceErrorCode implements ErrorCode {

    /** 工作空间选择参数不合法 */
    WORKSPACE_SELECTION_INVALID(40010, "工作空间选择参数不合法"),

    /** 无该工作空间的成员关系 */
    WORKSPACE_ACCESS_DENIED(40310, "无权进入该工作空间"),

    /** 成员已停用/被移除 */
    WORKSPACE_MEMBER_DISABLED(40311, "成员已停用或已被移除"),

    /** 租户冻结/注销/过期 */
    WORKSPACE_TENANT_UNAVAILABLE(40312, "租户已冻结、注销或已过期"),

    /** 店铺停用 */
    WORKSPACE_SHOP_UNAVAILABLE(40313, "店铺已停用"),

    /** 平台成员停用或无合规平台角色 */
    WORKSPACE_PLATFORM_UNAVAILABLE(40314, "无平台管理权限"),

    /** 同一会话并发切换 */
    WORKSPACE_SWITCH_BUSY(40910, "工作空间切换处理中，请稍后重试"),
    ;

    private final Integer code;
    private final String message;

    WorkspaceErrorCode(Integer code, String message) {
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

    /**
     * 工作空间不可用的错误码（可用于日志上下文）。
     */
    public static WorkspaceErrorCode unavailableFor(WorkspaceType type) {
        return switch (type) {
            case TENANT -> WORKSPACE_TENANT_UNAVAILABLE;
            case SHOP -> WORKSPACE_SHOP_UNAVAILABLE;
            case PLATFORM -> WORKSPACE_PLATFORM_UNAVAILABLE;
            default -> WORKSPACE_ACCESS_DENIED;
        };
    }
}
