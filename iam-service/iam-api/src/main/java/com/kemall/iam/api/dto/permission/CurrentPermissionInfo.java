package com.kemall.iam.api.dto.permission;

import com.kemall.iam.api.enums.WorkspaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 当前用户在当前工作空间下的权限快照。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentPermissionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private WorkspaceType workspaceType;

    private List<RoleInfo> roles;

    private List<PermissionInfo> permissions;

    private List<String> permCodes;
}
