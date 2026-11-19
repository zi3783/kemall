package com.kemall.iam.auth.domain.exception;

/**
 * 错误码接口
 * <p>
 * 由各子域的错误码枚举实现，统一对外暴露 code + message。
 */
public interface ErrorCode {

    Integer getCode();

    String getMessage();
}