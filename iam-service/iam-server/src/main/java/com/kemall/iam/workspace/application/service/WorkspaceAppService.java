package com.kemall.iam.workspace.application.service;

import com.kemall.iam.api.dto.auth.TokenInfo;
import com.kemall.iam.api.dto.workspace.SwitchWorkspaceRequest;
import com.kemall.iam.api.dto.workspace.SwitchWorkspaceResponse;
import com.kemall.iam.api.dto.workspace.WorkspaceInfo;
import com.kemall.iam.api.enums.WorkspaceType;
import com.kemall.iam.auth.application.port.JwtTokenService;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.domain.exception.SessionInvalidException;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.auth.domain.model.TokenClaims;
import com.kemall.iam.workspace.domain.exception.WorkspaceSelectionInvalidException;
import com.kemall.iam.workspace.domain.exception.WorkspaceSwitchBusyException;
import com.kemall.iam.workspace.domain.model.Workspace;
import com.kemall.iam.workspace.domain.repository.WorkspaceRepository;
import com.kemall.iam.workspace.domain.service.WorkspaceSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 工作空间应用服务。
 * <p>
 * 编排职责：列表/当前查询、切换工作空间、同步 Redis 会话上下文、重新签发 Token。
 * 准入规则在 {@link WorkspaceSelector}，会话与 Token 细节在 auth 子域的端口后面。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceAppService {

    /** 与 Refresh 共用同一把会话锁，避免并发轮换冲突 */
    private static final String SESSION_LOCK_PREFIX = "iam:lock:refresh:";

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceSelector workspaceSelector;
    private final SessionStore sessionStore;
    private final JwtTokenService jwtTokenService;
    private final RedissonClient redissonClient;

    @Value("${iam.jwt.refresh-expiration}")
    private long refreshExpirationSec;

    /**
     * 我可见的工作空间列表。
     */
    public List<WorkspaceInfo> listWorkspaces(Long userId) {
        return workspaceRepository.findAvailableWorkspaces(userId)
                .stream()
                .map(this::toInfo)
                .toList();
    }

    /**
     * 当前会话的工作空间，未选择或已失效返回 NONE。
     */
    public WorkspaceInfo currentWorkspace(Long userId, String sessionId) {
        AuthSession session = requireSession(userId, sessionId);
        return workspaceSelector
                .resolveCurrent(userId, session.getTenantId(), session.getShopId(), session.getPlatformMemberId())
                .map(this::toInfo)
                .orElseGet(WorkspaceAppService::noneWorkspace);
    }

    /**
     * 切换工作空间：校验准入 → 更新会话上下文 → 重新签发 Access/Refresh。
     */
    public SwitchWorkspaceResponse switchWorkspace(Long userId, String sessionId, SwitchWorkspaceRequest request) {
        WorkspaceType type = request.getWorkspaceType();
        if (type == null || WorkspaceType.NONE == type) {
            throw new WorkspaceSelectionInvalidException();
        }

        RLock lock = redissonClient.getLock(SESSION_LOCK_PREFIX + sessionId);
        boolean locked;
        try {
            locked = lock.tryLock(500L, 15_000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkspaceSwitchBusyException();
        }
        if (!locked) {
            throw new WorkspaceSwitchBusyException();
        }

        try {
            AuthSession session = requireSession(userId, sessionId);

            Workspace workspace = switch (type) {
                case TENANT -> workspaceSelector.selectTenant(userId, request.getTenantId());
                case SHOP -> workspaceSelector.selectShop(userId, request.getTenantId(), request.getShopId());
                case PLATFORM -> workspaceSelector.selectPlatform(userId);
                default -> throw new WorkspaceSelectionInvalidException();
            };
            workspace.setType(type);

            // 平台工作空间不携带租户/店铺上下文
            Long tenantId = workspace.isPlatform() ? null : workspace.getTenantId();
            Long shopId = workspace.isPlatform() ? null : workspace.getShopId();
            Long memberId = workspace.isPlatform() ? null : workspace.getMemberId();
            Long platformMemberId = workspace.isPlatform() ? workspace.getPlatformMemberId() : null;

            TokenClaims claims = TokenClaims.builder()
                    .userId(userId)
                    .sessionId(sessionId)
                    .tenantId(tenantId)
                    .shopId(shopId)
                    .memberId(memberId)
                    .platformMemberId(platformMemberId)
                    .build();
            TokenInfo tokenInfo = jwtTokenService.createToken(claims);

            // 同步 Redis Session 上下文，并轮换 Refresh 哈希
            applyContext(session, type, tenantId, shopId, memberId, platformMemberId);
            sessionStore.updateSession(session);
            sessionStore.saveRefreshTokenHash(sessionId, tokenInfo.getRefreshToken());

            log.info("[WORKSPACE] 切换成功 userId={} sessionId={} type={} tenantId={} shopId={}",
                    userId, sessionId, type, tenantId, shopId);

            return SwitchWorkspaceResponse.builder()
                    .workspace(toInfo(workspace))
                    .tokenInfo(tokenInfo)
                    .build();

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ==================== Utils ====================

    private AuthSession requireSession(Long userId, String sessionId) {
        AuthSession session = sessionStore.getSession(sessionId);
        if (session == null
                || session.isRevoked()
                || session.getExpiresAt() == null
                || session.getExpiresAt().isBefore(LocalDateTime.now())
                || session.getUserId() == null
                || !session.getUserId().equals(userId)) {
            throw new SessionInvalidException();
        }
        return session;
    }

    private void applyContext(AuthSession session,
                              WorkspaceType type,
                              Long tenantId,
                              Long shopId,
                              Long memberId,
                              Long platformMemberId) {
        LocalDateTime now = LocalDateTime.now();
        session.setTenantId(tenantId);
        session.setShopId(shopId);
        session.setMemberId(memberId);
        session.setPlatformMemberId(platformMemberId);
        session.setScopeType(type.name());
        session.setLastActiveAt(now);
        // 与会话 TTL 对齐，保证新 Refresh Token 在会话有效期内可用
        session.setExpiresAt(now.plusSeconds(refreshExpirationSec));
    }

    private WorkspaceInfo toInfo(Workspace workspace) {
        return WorkspaceInfo.builder()
                .workspaceType(workspace.getType())
                .tenantId(workspace.getTenantId())
                .tenantName(workspace.getTenantName())
                .shopId(workspace.getShopId())
                .shopName(workspace.getShopName())
                .memberId(workspace.getMemberId())
                .memberType(workspace.getMemberType())
                .platformMemberId(workspace.getPlatformMemberId())
                .build();
    }

    private static WorkspaceInfo noneWorkspace() {
        return WorkspaceInfo.builder().workspaceType(WorkspaceType.NONE).build();
    }
}
