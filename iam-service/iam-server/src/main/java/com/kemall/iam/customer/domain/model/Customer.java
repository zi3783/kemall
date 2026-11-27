package com.kemall.iam.customer.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 消费者身份聚合根（customer）。
 * <p>
 * 与 tenant_member / platform_member 平级：同一全局账号可兼具多重身份。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Long id;

    private Long userId;

    /** 1 active 2 disabled */
    private Integer status;

    private LocalDateTime createdAt;

    public boolean isActive() {
        return status != null && status == 1;
    }
}
