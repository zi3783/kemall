package com.kemall.iam.auth.application.service;

import com.kemall.iam.api.dto.auth.ChangePasswordRequest;
import com.kemall.iam.api.dto.auth.LogoutRequest;
import com.kemall.iam.api.dto.auth.RefreshTokenRequest;
import com.kemall.iam.api.dto.auth.SessionInfo;
import com.kemall.iam.api.dto.auth.TokenInfo;
import com.kemall.iam.api.enums.LogoutType;
import com.kemall.iam.auth.application.port.JwtTokenService;
import com.kemall.iam.auth.application.port.PasswordEncoder;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.application.support.SecurityPrincipalHolder;
import com.kemall.iam.auth.domain.exception.PasswordMismatchException;
import com.kemall.iam.auth.domain.exception.PasswordSameAsOldException;
import com.kemall.iam.auth.domain.exception.RefreshBusyException;
import com.kemall.iam.auth.domain.exception.RefreshTokenReusedException;
import com.kemall.iam.auth.domain.exception.SessionInvalidException;
import com.kemall.iam.auth.domain.exception.TokenExpiredException;
import com.kemall.iam.auth.domain.exception.TokenInvalidException;
import com.kemall.iam.auth.domain.exception.TokenPurposeInvalidException;
import com.kemall.iam.auth.domain.exception.UserNotFoundException;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.auth.domain.model.TokenClaims;
import com.kemall.iam.auth.domain.model.User;
import com.kemall.iam.auth.domain.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IAM 会话/密码/刷新/登出 应用服务。
 * <p>
 * 严格遵循 DDD：
 * <ul>
 *   <li>依赖方向：只依赖 domain.port / domain.repository / domain.exception / SecurityPrincipalHolder</li>
 *   <li>不做 JSON/SQL/Redis 细节（那些沉到 infrastructure）</li>
 *   <li>抛领域异常，由 web 层全局异常处理器统一映射</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionAppService {

    private final JwtTokenService jwtTokenService;
    private final SessionStore sessionStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;

    @Value("${iam.jwt.refresh-expiration}")
    private long refreshExpirationSec;

    private static final String REFRESH_LOCK_PREFIX = "iam:lock:refresh:";

    // ==================== Refresh ====================

    /**
     * 刷新 Token：校验旧 Refresh → 轮换 → 延长会话 → 返回新 TokenInfo。
     */
    public TokenInfo refresh(RefreshTokenRequest request) {
        // 1. 解析
        TokenClaims claims;
        try {
            claims = jwtTokenService.parseToken(request.getRefreshToken());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException e) {
            throw new TokenInvalidException();
        }

        // 2. 用途校验
        if (claims.getTokenPurpose() == null || !"REFRESH".equals(claims.getTokenPurpose())) {
            throw new TokenPurposeInvalidException();
        }

        String sessionId = claims.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new TokenInvalidException();
        }

        // 3. 抢会话级分布式锁，防止并发刷新误判
        RLock lock = redissonClient.getLock(REFRESH_LOCK_PREFIX + sessionId);
        boolean locked;
        try {
            locked = lock.tryLock(500L, 15_000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RefreshBusyException();
        }
        if (!locked) {
            throw new RefreshBusyException();
        }

        try {
            // 4. 再次校验会话（锁内）
            AuthSession session = sessionStore.getSession(sessionId);
            if (session == null
                    || session.isRevoked()
                    || session.getExpiresAt() == null
                    || session.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new SessionInvalidException();
            }
            if (!claims.getUserId().equals(session.getUserId())) {
                throw new SessionInvalidException();
            }

            // 5. Refresh 轮换：哈希比对
            if (!sessionStore.matchesRefreshToken(sessionId, request.getRefreshToken())) {
                // 重放！撤销整个会话，强制所有设备重新登录
                log.warn("Refresh Token 重放，撤销会话 sessionId={} userId={}", sessionId, session.getUserId());
                sessionStore.revokeSession(sessionId);
                throw new RefreshTokenReusedException();
            }

            // 6. 生成新 Token
            TokenClaims newClaims = TokenClaims.builder()
                    .userId(session.getUserId())
                    .sessionId(session.getSessionId())
                    .tenantId(session.getTenantId())
                    .shopId(session.getShopId())
                    .memberId(session.getMemberId())
                    .platformMemberId(session.getPlatformMemberId())
                    .build();
            TokenInfo tokenInfo = jwtTokenService.createToken(newClaims);

            // 7. 轮换：覆盖 Refresh 哈希 + 延长会话
            sessionStore.saveRefreshTokenHash(sessionId, tokenInfo.getRefreshToken());

            AuthSession updated = sessionStore.getSession(sessionId);
            if (updated != null) {
                updated.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationSec));
                sessionStore.updateSession(updated);
            }

            return tokenInfo;

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ==================== Logout ====================

    /**
     * 退出登录。CURRENT_DEVICE = 仅当前会话；ALL_DEVICES = 撤销全部。
     */
    public void logout(LogoutRequest request) {
        Long userId = SecurityPrincipalHolder.currentUserId();
        String sessionId = SecurityPrincipalHolder.currentSessionId();

        if (request.getLogoutType() == LogoutType.ALL_DEVICES) {
            sessionStore.revokeAllSessions(userId);
        } else {
            sessionStore.revokeSession(sessionId);
        }
    }

    // ==================== Sessions ====================

    /**
     * 当前登录用户的会话/设备列表。
     */
    public List<SessionInfo> listSessions() {
        Long userId = SecurityPrincipalHolder.currentUserId();
        String currentSessionId = SecurityPrincipalHolder.currentSessionId();

        List<AuthSession> sessions = sessionStore.listSessions(userId);
        List<SessionInfo> result = new ArrayList<>(sessions.size());
        for (AuthSession s : sessions) {
            result.add(toInfo(s, currentSessionId));
        }
        // 最后活跃时间倒序
        result.sort((a, b) -> {
            if (a.getLastActiveAt() == null) return 1;
            if (b.getLastActiveAt() == null) return -1;
            return b.getLastActiveAt().compareTo(a.getLastActiveAt());
        });
        return result;
    }

    /**
     * 踢出指定会话。必须是当前用户自己的会话。
     */
    public void kickSession(String sessionId) {
        Long userId = SecurityPrincipalHolder.currentUserId();

        AuthSession target = sessionStore.getSession(sessionId);
        if (target == null
                || target.isRevoked()
                || target.getExpiresAt() == null
                || target.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new SessionInvalidException();
        }
        if (!target.getUserId().equals(userId)) {
            // 越权踢人
            throw new SessionInvalidException();
        }
        sessionStore.revokeSession(sessionId);
    }

    // ==================== Change Password ====================

    /**
     * 修改密码。保留当前会话，撤销其他所有会话。
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new PasswordSameAsOldException();
        }

        Long userId = SecurityPrincipalHolder.currentUserId();
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }

        String newHash = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePasswordHash(userId, newHash);

        // 撤销除当前会话外的所有会话
        sessionStore.revokeOtherSessions(userId, SecurityPrincipalHolder.currentSessionId());

        log.info("用户密码修改完成 userId={}", userId);
    }

    // ==================== Utils ====================

    private SessionInfo toInfo(AuthSession s, String currentSessionId) {
        return SessionInfo.builder()
                .sessionId(s.getSessionId())
                .clientType(s.getClientType())
                .deviceId(s.getDeviceId())
                .deviceName(s.getDeviceName())
                .loginIp(s.getLoginIp())
                .userAgent(s.getUserAgent())
                .createdAt(s.getCreatedAt())
                .lastActiveAt(s.getLastActiveAt())
                .expiresAt(s.getExpiresAt())
                .current(s.getSessionId().equals(currentSessionId))
                .build();
    }
}
