package com.kemall.iam.platform.infrastructure.persistence.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台成员列表查询行（platform_member LEFT JOIN sys_user）。
 */
@Data
public class PlatformMemberRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Integer status;

    private LocalDateTime createdAt;

    private String username;

    private String nickname;
}
