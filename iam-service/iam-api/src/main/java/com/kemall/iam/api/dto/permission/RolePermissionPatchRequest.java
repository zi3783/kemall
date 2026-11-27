package com.kemall.iam.api.dto.permission;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色-权限增量关联请求（PATCH 语义）。
 * <p>
 * addPermCodes / removePermCodes 至少有一个非空。
 */
@Data
public class RolePermissionPatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 新增关联的权限编码 */
    private List<String> addPermCodes;

    /** 移除关联的权限编码 */
    private List<String> removePermCodes;

    @AssertTrue(message = "addPermCodes 与 removePermCodes 不能同时为空")
    public boolean isAnyChangePresent() {
        return (addPermCodes != null && !addPermCodes.isEmpty())
                || (removePermCodes != null && !removePermCodes.isEmpty());
    }
}
