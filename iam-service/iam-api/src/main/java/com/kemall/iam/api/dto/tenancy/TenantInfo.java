package com.kemall.iam.api.dto.tenancy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String tenantCode;

    private String tenantName;

    private String planCode;

    private Integer status;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}
