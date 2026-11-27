package com.kemall.iam.tenancy.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户/店铺成员持久化对象（tenant_member）。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tenant_member")
public class TenantMemberPO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long shopId;

    private Long userId;

    private Integer memberType;

    private Integer status;

    private LocalDateTime joinedAt;

    private LocalDateTime deletedAt;
}
