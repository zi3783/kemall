package com.kemall.iam.api.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionInfo {

    private String sessionId;

    private String clientType;

    private String deviceId;

    private String deviceName;

    private String loginIp;

    private String userAgent;

    private LocalDateTime createdAt;

    private LocalDateTime lastActiveAt;

    private LocalDateTime expiresAt;

    private Boolean current;
}
