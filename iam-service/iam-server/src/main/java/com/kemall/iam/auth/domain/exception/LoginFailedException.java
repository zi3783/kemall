package com.kemall.iam.auth.domain.exception;

import java.util.Map;

/**
 * 登录失败异常
 * <p>
 * 安全设计：不区分"账号不存在"与"密码错误"，统一返回模糊提示，
 * 防止攻击者通过提示信息枚举有效账号。
 * <p>
 * 注意：用户名仅用于内部日志与风控统计，<b>不应回传给前端</b>。
 */
public class LoginFailedException extends AuthDomainException {

    public LoginFailedException(String username) {
        super(AuthErrorCode.LOGIN_FAILED,
                Map.of("username", maskUsername(username)));
    }

    /**
     * 掩码处理，防止日志泄漏完整用户名
     * 例如：alice -> a***e
     */
    private static String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "**";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }
}