package com.kemall.iam.api.dto.workspace;

import com.kemall.iam.api.enums.WorkspaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 工作空间信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private WorkspaceType workspaceType;

    private Long tenantId;

    private String tenantName;

    /** 0 表示租户级工作空间 */
    private Long shopId;

    private String shopName;

    /** 对应 tenant_member.id */
    private Long memberId;

    private Integer memberType;

    /** 对应 platform_member.id，仅平台工作空间有值 */
    private Long platformMemberId;
}
