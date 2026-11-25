package com.kemall.iam.api.enums;

/**
 * 验证码使用场景。
 */
public enum VerificationScene {
    /** 注册 */
    REGISTER,
    /** 密码重置 */
    RESET_PASSWORD,
    /** 绑定手机号/邮箱 */
    BIND_IDENTITY,
    /** 解绑手机号/邮箱 */
    UNBIND_IDENTITY
}
