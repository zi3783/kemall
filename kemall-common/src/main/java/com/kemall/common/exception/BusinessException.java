package com.kemall.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    private final String message;
    
    /**
     * 自定义错误码和消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 默认错误码 500
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
    
    /**
     * 带原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}