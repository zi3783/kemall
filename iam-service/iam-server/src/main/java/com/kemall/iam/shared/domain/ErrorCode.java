package com.kemall.iam.shared.domain;

/**
 * 错误码接口（共享内核）。
 * <p>
 * 由各子域的错误码枚举实现，统一对外暴露 code + message。
 * 这样多个子域（auth、user 等）可以共享同一套异常契约，
 * 而不需要跨子域依赖各自领域的异常包。
 */
public interface ErrorCode {

    Integer getCode();

    String getMessage();
}
