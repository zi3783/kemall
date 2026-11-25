package com.kemall.iam.user.application.port;

import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;

/**
 * 验证码发送端口。
 * <p>
 * 开发环境用日志实现；生产可替换为短信/邮件服务适配器。
 */
public interface VerificationCodeSender {

    void send(IdentityType type, String target, String code, VerificationScene scene);
}
