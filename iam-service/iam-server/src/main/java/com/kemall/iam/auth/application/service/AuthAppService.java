
package com.kemall.iam.auth.application.service;

import com.kemall.iam.api.dto.auth.LoginRequest;
import com.kemall.iam.api.dto.auth.LoginResponse;
import com.kemall.iam.api.dto.auth.LoginUserInfo;
import com.kemall.iam.api.dto.auth.TokenInfo;
import com.kemall.iam.auth.application.port.JwtTokenService;
import com.kemall.iam.auth.application.port.PasswordEncoder;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.domain.exception.AccountNotLoginAllowedException;
import com.kemall.iam.auth.domain.exception.AuthErrorCode;
import com.kemall.iam.auth.domain.exception.LoginFailedException;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.auth.domain.model.TokenClaims;
import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthAppService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;

    private final SessionStore sessionStore;

    @Value("${iam.jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /**
     * 用户密码登录
     */
    public LoginResponse login(LoginRequest request) {

        // 1. 根据账号类型查询用户
        User user = userRepository.findByAccount(
                request.getAccount(),
                request.getAccountType()
        );

        // 2. 校验账号和密码
        // 对外统一返回账号或密码错误，避免账号枚举
        if (user == null ||
                !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                )) {

            throw new LoginFailedException(request.getAccount());
        }

        // 3. 校验用户状态
        if (!user.canLogin()) {
            throw new AccountNotLoginAllowedException(user.getId(), AuthErrorCode.ACCOUNT_NOT_LOGIN_ALLOWED.getMessage());
        }

        // 6. 创建 Session ID
        String sessionId = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();

        // 7. 创建 Session
        AuthSession session = AuthSession.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .clientType(request.getClientType().name())
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .createdAt(now)
                .expiresAt(now.plusSeconds(refreshTokenExpiration))
                .revoked(false)
                .build();

        sessionStore.createSession(session);

        try {

            // 8. 构建 JWT Claims
            TokenClaims claims = TokenClaims.builder()
                    .userId(user.getId())
                    .sessionId(sessionId)
                    .build();

            // 9. 创建 Access Token 和 Refresh Token
            TokenInfo tokenInfo = jwtTokenService.createToken(claims);

            // 10. Redis 只保存 Refresh Token 哈希
            sessionStore.saveRefreshTokenHash(
                    sessionId,
                    tokenInfo.getRefreshToken()
            );

            // 11. 更新最后登录时间
            userRepository.updateLastLoginTime(
                    user.getId(),
                    LocalDateTime.now()
            );

            // 13. 构建用户信息
            LoginUserInfo userInfo = LoginUserInfo.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatarUrl(user.getAvatarUrl())
                    .build();

            // 14. 返回登录结果
            return LoginResponse.builder()
                    .userInfo(userInfo)
                    .tokenInfo(tokenInfo)
                    .build();

        } catch (RuntimeException e) {

            // 发生异常时撤销刚创建的 Session
            sessionStore.revokeSession(sessionId);

            throw e;
        }
    }
}
