package com.kemall.iam.auth.application.service;

import com.kemall.iam.api.dto.auth.LoginRequest;
import com.kemall.iam.api.dto.auth.LoginResponse;
import com.kemall.iam.api.dto.auth.LoginUserInfo;
import com.kemall.iam.api.dto.auth.TokenInfo;
import com.kemall.iam.api.enums.UserType;
import com.kemall.iam.auth.application.port.JwtTokenService;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.domain.exception.AccountNotLoginAllowedException;
import com.kemall.iam.auth.domain.exception.LoginFailedException;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.auth.domain.model.LoginIdentity;
import com.kemall.iam.auth.domain.model.TokenClaims;
import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.auth.domain.repository.LoginIdentityRepository;
import com.kemall.iam.auth.domain.repository.UserRepository;
import com.kemall.iam.auth.infrastructure.security.AccountAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 登录应用服务（DDD Application Service）。
 * <p>
 * Spring Security 体系接入后的编排职责：
 * <ol>
 *   <li>调用 {@link AuthenticationManager#authenticate()} 完成"认证"（由
 *       {@link com.kemall.iam.auth.infrastructure.security.AccountAuthenticationProvider}
 *       委派给领域端口执行查用户 / 密码校验 / 状态校验）</li>
 *   <li>捕获 Spring Security 标准异常，ACL 映射为领域异常</li>
 *   <li>认证成功后继续业务逻辑：创建 Redis Session + 签发 JWT + 保存 Refresh 哈希 + 更新最后登录时间</li>
 * </ol>
 * <p>
 * 注意：Session 创建 / JWT 签发 / Refresh 轮换 是业务逻辑，
 * 不交给 Spring Security 框架，保持 DDD 纯净性。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthAppService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final SessionStore sessionStore;
    private final UserRepository userRepository;
    private final LoginIdentityRepository loginIdentityRepository;

    @Value("${iam.jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /**
     * 用户密码登录。
     *
     * @param request        登录请求（含账号、类型、密码、设备信息）
     * @param servletRequest 用于提取登录 IP / User-Agent 元数据
     */
    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {

        // 1. 调 Spring Security 认证链路
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new AccountAuthenticationToken(
                            request.getAccount(),
                            request.getPassword(),
                            request.getAccountType()));
        } catch (BadCredentialsException e) {
            // 账号不存在 / 密码错误：统一模糊提示，防账号枚举
            throw new LoginFailedException(request.getAccount());
        } catch (DisabledException e) {
            // 账户禁用（canLogin() 失败）
            throw new AccountNotLoginAllowedException(null, "DISABLED");
        } catch (AuthenticationException e) {
            // 其他 Spring Security 认证异常（LockedException/CredentialsExpiredException 等）
            // AccountAuthenticationProvider 当前只抛 BadCredentials + Disabled，
            // 这里兜底保护
            log.warn("[IAM] 未预期的 Spring Security 认证异常", e);
            throw new LoginFailedException(request.getAccount());
        }

        // 2. 从 principal 取领域 User（AccountAuthenticationProvider 返回的 principal）
        if (!(auth.getPrincipal() instanceof User user)) {
            log.error("[IAM] 认证成功但 principal 类型异常: {}",
                    auth.getPrincipal() == null ? "null" : auth.getPrincipal().getClass());
            throw new LoginFailedException(request.getAccount());
        }

        // 3. 创建 Redis Session（业务逻辑，Spring Security 不负责）
        String sessionId = java.util.UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        AuthSession session = AuthSession.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .clientType(request.getClientType().name())
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .loginIp(extractClientIp(servletRequest))
                .userAgent(servletRequest == null ? null : servletRequest.getHeader("User-Agent"))
                .createdAt(now)
                .lastActiveAt(now)
                .expiresAt(now.plusSeconds(refreshTokenExpiration))
                .revoked(false)
                .build();

        sessionStore.createSession(session);

        try {
            // 4. 签发 JWT
            TokenClaims claims = TokenClaims.builder()
                    .userId(user.getId())
                    .sessionId(sessionId)
                    .build();
            TokenInfo tokenInfo = jwtTokenService.createToken(claims);

            // 5. 保存 Refresh Token 哈希（轮换机制根基）
            sessionStore.saveRefreshTokenHash(sessionId, tokenInfo.getRefreshToken());

            // 6. 更新最后登录时间
            userRepository.updateLastLoginTime(user.getId(), now);

            // 7. 组装返回（含身份类型：消费者无需选择工作空间）
            LoginIdentity identity = loginIdentityRepository.resolve(user.getId());
            List<UserType> userTypes = new ArrayList<>();
            if (identity.customer()) {
                userTypes.add(UserType.CUSTOMER);
            }
            if (identity.tenantMembershipCount() > 0) {
                userTypes.add(UserType.TENANT_MEMBER);
            }
            if (identity.platformMember()) {
                userTypes.add(UserType.PLATFORM_MEMBER);
            }

            LoginUserInfo userInfo = LoginUserInfo.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatarUrl(user.getAvatarUrl())
                    .userTypes(userTypes)
                    .workspaceRequired(identity.workspaceRequired())
                    .build();

            return LoginResponse.builder()
                    .userInfo(userInfo)
                    .tokenInfo(tokenInfo)
                    .build();

        } catch (RuntimeException e) {
            // 业务任一步失败 → 撤销刚创建的 Session，避免脏会话残留
            sessionStore.revokeSession(sessionId);
            throw e;
        }
    }

    // ==================== Utils ====================

    private static String extractClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
