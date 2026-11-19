package com.kemall.iam.tenant.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 租户/店铺成员
 * </p>
 *
 * @author author
 * @since 2026-11-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tenant_member")
public class TenantMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    /**
     * 0 tenant scope
     */
    private Long shopId;

    private Long userId;

    /**
     * 1 buyer 2 staff 3 tenant admin
     */
    private Integer memberType;

    private Integer status;

    private LocalDateTime joinedAt;

    private LocalDateTime deletedAt;


}
