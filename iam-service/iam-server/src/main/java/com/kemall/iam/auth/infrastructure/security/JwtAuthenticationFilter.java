package com.kemall.iam.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.auth.application.model.LoginPrincipal;
import com.kemall.iam.auth.application.port.JwtTokenService;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.auth.application.port.UserAuthorityProvider;
import com.kemall.iam.auth.domain.exception.AuthErrorCode;
import com.kemall.iam.auth.domain.model.AuthSession;
import com.kemall.iam.auth.domain.model.TokenClaims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JWT 认证过滤器。
 * <p>
 * 流水线（DDD 领域端口调用）：
 * <ol>
 *   <li>提取 {@code Authorization: Bearer xxx}，缺失直接放行（交给后续 Filter/EntryPoint 处理 401）</li>
 *   <li>调用 {@link JwtTokenService#parseToken(String)} 验签/过期</li>
 *   <li>校验 tokenPurpose == ACCESS（Refresh Token 不能访问业务接口）</li>
 *   <li>调用 {@link SessionStore#getSession(String)} 查 Redis 会话
 *       （存在、未撤销、未过期、userId 与 token 一致）</li>
 *   <li>构造 {@link LoginPrincipal} 写入 SecurityContext</li>
 * </ol>
 * 任何一步失败 → 直接写 HTTP 401 + ApiResult JSON 并返回，
 * 不进入后续 Filter。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final SessionStore sessionStore;
    private final UserAuthorityProvider userAuthorityProvider;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private List<String> whiteList = List.of();

    /**
     * 由 {@link SecurityConfig} 在构造时注入白名单。
     */
    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList == null ? List.of() : List.copyOf(whiteList);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header == null || header.isBlank() || !header.startsWith(BEARER_PREFIX)) {
            // 无 token：放行，让后续 Filter 处理（/login /refresh 已通过 shouldNotFilter 排除）
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        TokenClaims claims;
        try {
            claims = jwtTokenService.parseToken(token);
        } catch (JwtException e) {
            log.info("JWT 解析失败: {}", e.getMessage());
            write401(response, AuthErrorCode.TOKEN_INVALID);
            return;
        } catch (Exception e) {
            log.warn("JWT 解析异常", e);
            write401(response, AuthErrorCode.TOKEN_INVALID);
            return;
        }

        // 用途校验：业务接口只接受 ACCESS Token
        if (claims.getTokenPurpose() == null
                || !"ACCESS".equals(claims.getTokenPurpose())) {
            write401(response, AuthErrorCode.TOKEN_PURPOSE_INVALID);
            return;
        }

        // Session 有效性校验
        String sessionId = claims.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            write401(response, AuthErrorCode.TOKEN_INVALID);
            return;
        }
        AuthSession session = sessionStore.getSession(sessionId);
        if (session == null
                || session.isRevoked()
                || session.getExpiresAt() == null
                || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            write401(response, AuthErrorCode.SESSION_INVALID);
            return;
        }
        if (session.getUserId() == null
                || !session.getUserId().equals(claims.getUserId())) {
            write401(response, AuthErrorCode.SESSION_INVALID);
            return;
        }

        // 装载当前工作空间的权限编码（失败时降级为无额外权限，方法级鉴权会拒绝）
        List<String> authorities;
        try {
            authorities = List.copyOf(userAuthorityProvider.loadAuthorities(session));
        } catch (Exception e) {
            log.error("加载会话权限失败 sessionId={}", sessionId, e);
            authorities = List.of();
        }

        // 构造 Principal 并写入 SecurityContext
        SecurityContextHolder.getContext()
                .setAuthentication(LoginPrincipal.fromSession(session, authorities));
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void write401(HttpServletResponse response, AuthErrorCode code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.fail(code.getCode(), code.getMessage())));
    }
}
