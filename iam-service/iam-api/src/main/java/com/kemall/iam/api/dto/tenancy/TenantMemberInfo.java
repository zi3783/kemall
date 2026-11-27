package com.kemall.iam.api.dto.tenancy;

import com.kemall.iam.api.enums.MemberType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户/店铺成员信息（含账号展示字段）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMemberInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    /** 0 表示租户级成员 */
    private Long shopId;

    private Long userId;

    private String username;

    private String nickname;

    private MemberType memberType;

    private Integer status;

    private LocalDateTime joinedAt;
}
