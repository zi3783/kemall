package com.kemall.iam.platform.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 平台管理员成员聚合根（platform_member）。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformMember {

    private Long id;

    private Long userId;

    /** 1 active 2 disabled */
    private Integer status;

    private LocalDateTime createdAt;

    public boolean isActive() {
        return status != null && status == 1;
    }
}
