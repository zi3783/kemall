package com.kemall.iam.api.dto.workspace;

import com.kemall.iam.api.enums.WorkspaceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 切换工作空间请求。
 * <p>
 * 字段要求由 type 决定：
 * <ul>
 *   <li>TENANT：tenantId 必填</li>
 *   <li>SHOP：tenantId、shopId 必填</li>
 *   <li>PLATFORM：两者忽略</li>
 * </ul>
 */
@Data
public class SwitchWorkspaceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "工作空间类型不能为空")
    private WorkspaceType workspaceType;

    private Long tenantId;

    private Long shopId;
}
