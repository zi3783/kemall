package com.kemall.iam.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.auth.domain.exception.AuthErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 已认证但无权限统一返回 403 JSON。
 * <p>
 * 本期只做预留；后续启用 {@code @PreAuthorize} 后由它捕获。
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResult.fail(AuthErrorCode.PERMISSION_DENIED.getCode(),
                        AuthErrorCode.PERMISSION_DENIED.getMessage())));
    }
}
