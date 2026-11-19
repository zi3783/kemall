
package com.kemall.iam.auth.infrastructure.security;

import com.kemall.iam.api.dto.auth.TokenInfo;
import com.kemall.iam.auth.application.port.JwtTokenService;
import com.kemall.iam.auth.domain.model.TokenClaims;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenServiceImpl implements JwtTokenService {

    private final SecretKey secretKey;

    private final long accessTokenExpiration;

    private final long refreshTokenExpiration;

    private final String issuer;

    private final String audience;

    public JwtTokenServiceImpl(
            @Value("${iam.jwt.secret}") String secret,
            @Value("${iam.jwt.access-expiration}") long accessTokenExpiration,
            @Value("${iam.jwt.refresh-expiration}") long refreshTokenExpiration,
            @Value("${iam.jwt.issuer}") String issuer,
            @Value("${iam.jwt.audience}") String audience) {

        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    public String generateAccessToken(TokenClaims claims) {
        return generateToken(
                claims,
                "ACCESS",
                accessTokenExpiration
        );
    }

    @Override
    public String generateRefreshToken(TokenClaims claims) {
        return generateToken(
                claims,
                "REFRESH",
                refreshTokenExpiration
        );
    }

    private String generateToken(
            TokenClaims claims,
            String tokenPurpose,
            long expiration) {

        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(claims.getUserId()))
                .audience()
                .add(audience)
                .and()
                .claim("sessionId", claims.getSessionId())
                .claim("tenantId", claims.getTenantId())
                .claim("shopId", claims.getShopId())
                .claim("memberId", claims.getMemberId())
                .claim("platformMemberId", claims.getPlatformMemberId())
                .claim("tokenPurpose", tokenPurpose)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiration)))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public TokenClaims parseToken(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return TokenClaims.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .sessionId(claims.get("sessionId", String.class))
                .tenantId(toLong(claims.get("tenantId")))
                .shopId(toLong(claims.get("shopId")))
                .memberId(toLong(claims.get("memberId")))
                .platformMemberId(
                        toLong(claims.get("platformMemberId"))
                )
                .tokenPurpose(
                        claims.get("tokenPurpose", String.class)
                )
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return Long.valueOf(value.toString());
    }

    @Override
    public TokenInfo createToken(TokenClaims claims) {

        String accessToken = generateAccessToken(claims);

        String refreshToken = generateRefreshToken(claims);

        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .refreshExpiresIn(refreshTokenExpiration)
                .build();
    }
}
