package com.kemall.iam.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenClaims {

    private Long userId;

    private String sessionId;

    private Long tenantId;

    private Long shopId;

    private Long memberId;

    private Long platformMemberId;

    private String tokenType;

    private String tokenPurpose;
}