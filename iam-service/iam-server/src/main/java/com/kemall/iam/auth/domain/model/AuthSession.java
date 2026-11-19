package com.kemall.iam.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthSession {

    private String sessionId;

    private Long userId;

    private String clientType;

    private String deviceId;

    private String deviceName;

    /**
     * 当前租户上下文
     */
    private Long tenantId;

    /**
     * 当前店铺上下文，0 表示租户级
     */
    private Long shopId;

    /**
     * 当前租户成员 ID
     */
    private Long memberId;

    /**
     * 当前平台成员 ID
     */
    private Long platformMemberId;

    /**
     * NONE / TENANT / SHOP / PLATFORM
     */
    private String scopeType;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean revoked;

    /**
     * Refresh Token 的 SHA-256 哈希
     */
    private String refreshTokenHash;
}