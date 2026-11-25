package com.kemall.iam.user.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证码相关配置（iam.verification.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "iam.verification")
public class VerificationProperties {

    /** 验证码有效期（秒） */
    private long ttlSeconds = 300;

    /** 同一目标发送间隔（秒） */
    private long resendIntervalSeconds = 60;

    /** 最大错误次数 */
    private int maxAttempts = 5;

    /** 是否在响应中回显验证码（仅开发环境开启） */
    private boolean exposeCode = false;
}
