package com.kemall.iam.api.dto.permission;

import com.kemall.iam.api.enums.RoleScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private String roleCode;

    private String roleName;

    private RoleScopeType scopeType;

    private Integer status;
}
