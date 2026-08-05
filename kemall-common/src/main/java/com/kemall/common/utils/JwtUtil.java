package com.kemall.common.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Map<String, Object> claims){
        return Jwts.builder()
                .claims(claims)
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(Map<String,Object> claims, Long duration, TimeUnit timeUnit){
        Date expiration =  new Date(System.currentTimeMillis() + timeUnit.toMillis(duration));
        return Jwts.builder()
                .claims(claims)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        try{
            return Jwts.parser()
                    .verifyWith(secretKey)           // 验证签名
                    .build()
                    .parseSignedClaims(token)        // 解析
                    .getPayload();
        }catch (Exception e){
            log.info("token失效");
            return null;
        }
    }


}