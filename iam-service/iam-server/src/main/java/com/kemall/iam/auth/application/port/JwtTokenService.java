package com.kemall.iam.auth.application.port;

import com.kemall.iam.api.dto.auth.TokenInfo;
import com.kemall.iam.auth.domain.model.TokenClaims;

public interface JwtTokenService {

    String generateAccessToken(TokenClaims claims);

    String generateRefreshToken(TokenClaims claims);

    TokenClaims parseToken(String token);

    boolean validateToken(String token);

    TokenInfo createToken(TokenClaims claims);
}