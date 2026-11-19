package com.kemall.iam.api.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenInfo {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    // Access Token 有效期，单位秒
    private Long expiresIn;

    // Refresh Token 有效期，单位秒
    private Long refreshExpiresIn;
}