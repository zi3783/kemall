package com.kemall.iam.tenancy.infrastructure.persistence.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成员列表查询行（tenant_member LEFT JOIN sys_user）。
 */
@Data
public class TenantMemberRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private Long shopId;

    private Long userId;

    private Integer memberType;

    private Integer status;

    private LocalDateTime joinedAt;

    private String username;

    private String nickname;
}
