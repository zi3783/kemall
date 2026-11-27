package com.kemall.iam.api.dto.permission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 成员角色分配请求（全量覆盖）。
 */
@Data
public class MemberRoleAssignRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "角色列表不能为空")
    private List<Long> roleIds;
}
