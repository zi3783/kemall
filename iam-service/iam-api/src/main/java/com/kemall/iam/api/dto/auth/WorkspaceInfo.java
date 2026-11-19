package com.kemall.iam.api.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceInfo {

    private Long tenantId;

    private String tenantName;

    // 0 表示租户级工作空间
    private Long shopId;

    private String shopName;

    // 对应 tenant_member.id
    private Long memberId;

    private Integer memberType;

    private String workspaceType;
}