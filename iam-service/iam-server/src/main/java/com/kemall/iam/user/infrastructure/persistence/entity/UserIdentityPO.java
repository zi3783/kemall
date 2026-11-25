package com.kemall.iam.user.infrastructure.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * user_identity 持久化对象（user 子域）。
 */
@Data
public class UserIdentityPO {

    private Long id;

    private Long userId;

    private String identityType;

    private String identityValue;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;
}
