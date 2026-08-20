package com.kemall.common.enums;

public enum ResultCode {
    success(200, "success"),
    fail(500, "fail");

    private Integer code;
    private String msg;

    private ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
