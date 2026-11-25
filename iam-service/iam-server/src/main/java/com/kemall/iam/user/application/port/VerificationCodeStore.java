package com.kemall.iam.user.application.port;

import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;

/**
 * 验证码存储端口。
 */
public interface VerificationCodeStore {

    void save(VerificationScene scene, IdentityType type, String target, String code, long ttlSeconds);

    /**
     * 校验验证码；内部累计错误次数，超过上限则作废。
     */
    boolean verify(VerificationScene scene, IdentityType type, String target, String code, int maxAttempts);

    /**
     * 校验通过后消费（删除）验证码。
     */
    void consume(VerificationScene scene, IdentityType type, String target);

    /**
     * 申请发送配额（同一目标 N 秒内仅允许一次）。
     */
    boolean tryAcquireSendQuota(IdentityType type, String target, long seconds);
}
