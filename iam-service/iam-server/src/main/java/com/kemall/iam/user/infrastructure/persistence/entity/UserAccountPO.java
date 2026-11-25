package com.kemall.iam.user.infrastructure.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * sys_user 持久化对象（user 子域）。
 */
@Data
public class UserAccountPO {

    private Long id;

    private String username;

    private String passwordHash;

    private String nickname;

    private String avatarUrl;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
