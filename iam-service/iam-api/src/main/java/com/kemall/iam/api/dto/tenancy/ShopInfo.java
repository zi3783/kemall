package com.kemall.iam.api.dto.tenancy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private String shopCode;

    private String shopName;

    private String logoUrl;

    private Integer status;

    private String timezone;

    private LocalDateTime createdAt;
}
