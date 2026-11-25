package com.kemall.iam.web;

import com.kemall.iam.api.dto.common.ApiResult;
import com.kemall.iam.auth.domain.exception.AuthDomainException;
import com.kemall.iam.shared.domain.DomainException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理器（跨子域共享）。
 * <p>
 * 把领域异常/框架异常统一映射为 {@link ApiResult}，
 * 保持 IAM 对外契约的一致性。
 * <p>
 * 异常归类：
 * <ul>
 *   <li>{@link AuthDomainException} —— 认证子域业务异常，按错误码决定 HTTP 状态</li>
 *   <li>其他 {@link DomainException} —— 用户等其他子域业务异常，按错误码段映射</li>
 *   <li>参数校验异常 —— 400</li>
 *   <li>AccessDeniedException —— 403（Spring Security 直接抛出时的兜底）</li>
 *   <li>404 —— 404</li>
 *   <li>其他 —— 500</li>
 * </ul>
 */
@RestControllerAdvice(basePackages = "com.kemall.iam")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthDomainException.class)
    public ResponseEntity<ApiResult<Void>> handleAuth(AuthDomainException e) {
        logException(e);
        return ResponseEntity
                .status(httpStatusFor(e.getCode()))
                .body(ApiResult.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResult<Void>> handleDomain(DomainException e) {
        logException(e);
        return ResponseEntity
                .status(httpStatusFor(e.getCode()))
                .body(ApiResult.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResult<Void>> handleValidation(Exception e) {
        String msg = extractValidationMessage(e);
        log.warn("[IAM] 参数校验失败: {}", msg);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(40000, msg));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("[IAM] 访问被拒: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResult.fail(40300, "无操作权限"));
    }

    /**
     * 唯一键冲突兜底（并发注册/绑定场景）。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResult<Void>> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("[IAM] 数据唯一键冲突: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResult.fail(42100, "数据已存在，请勿重复提交"));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNotFound(NoHandlerFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResult.fail(40404, "接口不存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleOther(Exception e) {
        log.error("[IAM] 未处理异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail(50000, "系统繁忙，请稍后重试"));
    }

    // ==================== Utils ====================

    private HttpStatus httpStatusFor(Integer code) {
        if (code == null) return HttpStatus.BAD_REQUEST;
        // 40xxx 认证/凭证类
        if (code >= 40100 && code <= 40199) return HttpStatus.UNAUTHORIZED;
        if (code >= 40201 && code <= 40205) return HttpStatus.UNAUTHORIZED;
        if (code == 40206) return HttpStatus.CONFLICT;
        if (code >= 40300 && code <= 40399) return HttpStatus.FORBIDDEN;
        if (code >= 40400 && code <= 40499) return HttpStatus.NOT_FOUND;
        // 42xxx 用户子域
        if (code >= 42001 && code <= 42003) return HttpStatus.CONFLICT;
        if (code == 42005) return HttpStatus.TOO_MANY_REQUESTS;
        // 409xx 并发冲突（如工作空间切换）
        if (code >= 40900 && code <= 40999) return HttpStatus.CONFLICT;
        return HttpStatus.BAD_REQUEST;
    }

    private String extractValidationMessage(Exception e) {
        if (e instanceof MethodArgumentNotValidException mve && mve.getBindingResult().hasFieldErrors()) {
            return mve.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        if (e instanceof BindException be && be.getBindingResult().hasFieldErrors()) {
            return be.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        if (e instanceof ConstraintViolationException cve && !cve.getConstraintViolations().isEmpty()) {
            return cve.getConstraintViolations().iterator().next().getMessage();
        }
        return "参数校验失败";
    }

    private void logException(DomainException e) {
        if (e.isExpected()) {
            log.warn("[IAM] 业务异常 code={} msg={} ctx={}",
                    e.getCode(), e.getMessage(), e.getContext());
        } else {
            log.error("[IAM] 未预期业务异常 code={}", e.getCode(), e);
        }
    }
}
