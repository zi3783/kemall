package com.kemall.iam.auth.application.model;

import com.kemall.iam.auth.domain.model.AuthSession;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

/**
 * IAM 登录主体（Principal）。
 * <p>
 * 由 {@link com.kemall.iam.auth.infrastructure.security.JwtAuthenticationFilter}
 * 在成功校验 JWT + Redis Session 后构造，作为当前线程认证上下文的唯一身份载体。
 * <p>
 * 放在 application.model 而非 infrastructure：
 * 应用层/Web 层需要读取当前登录身份，避免"应用层反向依赖基础设施"。
 */
@Getter
public class LoginPrincipal extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String sessionId;
    private final String clientType;
    private final Long tenantId;
    private final Long shopId;
    private final Long memberId;
    private final Long platformMemberId;

    public LoginPrincipal(Long userId,
                          String sessionId,
                          String clientType,
                          Long tenantId,
                          Long shopId,
                          Long memberId,
                          Long platformMemberId) {
        super(userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_AUTHENTICATED")));
        this.userId = userId;
        this.sessionId = sessionId;
        this.clientType = clientType;
        this.tenantId = tenantId;
        this.shopId = shopId;
        this.memberId = memberId;
        this.platformMemberId = platformMemberId;
        setDetails(null);
    }

    public static LoginPrincipal fromSession(AuthSession session) {
        return new LoginPrincipal(
                session.getUserId(),
                session.getSessionId(),
                session.getClientType(),
                session.getTenantId(),
                session.getShopId(),
                session.getMemberId(),
                session.getPlatformMemberId()
        );
    }
}
