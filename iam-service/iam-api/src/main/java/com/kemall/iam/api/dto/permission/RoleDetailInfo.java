package com.kemall.iam.api.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 角色详情：角色本身 + 已关联权限。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private RoleInfo role;

    private List<PermissionInfo> permissions;
}
