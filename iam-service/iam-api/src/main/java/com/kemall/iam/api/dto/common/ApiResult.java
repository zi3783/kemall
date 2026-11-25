package com.kemall.iam.api.dto.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * IAM 统一响应体。
 * <p>
 * 注意：DTO 只承载契约，不与业务模块的异常体系耦合。
 * 领域层抛 {@link com.kemall.iam.auth.domain.exception.DomainException}，
 * 由 web 层的全局异常处理器负责映射为 ApiResult。
 */
@Getter
@Setter
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务码，成功 = 200 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    private ApiResult() {}

    public static <T> ApiResult<T> success() {
        return build(200, "success", null);
    }

    public static <T> ApiResult<T> success(T data) {
        return build(200, "success", data);
    }

    public static <T> ApiResult<T> success(String msg, T data) {
        return build(200, msg, data);
    }

    public static <T> ApiResult<T> fail(int code, String msg) {
        return build(code, msg, null);
    }

    public static <T> ApiResult<T> fail(String msg) {
        return build(500, msg, null);
    }

    private static <T> ApiResult<T> build(int code, String msg, T data) {
        ApiResult<T> r = new ApiResult<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        return r;
    }
}
