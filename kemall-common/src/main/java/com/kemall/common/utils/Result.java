package com.kemall.common.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result <T>  implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result(200, "success", null);
    }
    public static <T> Result<T> success(T data) {
        return new Result(200, "success", data);
    }
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }
    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }
    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

}
