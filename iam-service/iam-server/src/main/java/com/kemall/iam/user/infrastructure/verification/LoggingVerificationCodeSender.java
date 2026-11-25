package com.kemall.iam.user.infrastructure.verification;

import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;
import com.kemall.iam.user.application.port.VerificationCodeSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 开发环境验证码发送器：仅打印日志。
 * <p>
 * 生产环境应替换为短信/邮件适配器。
 */
@Component
@Slf4j
public class LoggingVerificationCodeSender implements VerificationCodeSender {

    @Override
    public void send(IdentityType type, String target, String code, VerificationScene scene) {
        log.info("[USER][验证码] scene={} type={} target={} code={}", scene, type, target, code);
    }
}
