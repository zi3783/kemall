package com.kemall.iam.user.application.service;

import com.kemall.iam.api.dto.user.SendVerificationCodeRequest;
import com.kemall.iam.api.dto.user.VerificationCodeResponse;
import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;
import com.kemall.iam.user.application.config.VerificationProperties;
import com.kemall.iam.user.application.port.VerificationCodeSender;
import com.kemall.iam.user.application.port.VerificationCodeStore;
import com.kemall.iam.user.application.support.IdentitySupport;
import com.kemall.iam.user.domain.exception.IdentityAlreadyBoundException;
import com.kemall.iam.user.domain.exception.VerificationCodeTooFrequentException;
import com.kemall.iam.user.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * 验证码应用服务。
 * <p>
 * 负责生成、频控、存储、投递；具体存储与发送通过端口下沉。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeAppService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserAccountRepository userAccountRepository;
    private final VerificationCodeStore codeStore;
    private final VerificationCodeSender codeSender;
    private final VerificationProperties properties;

    public VerificationCodeResponse send(SendVerificationCodeRequest request) {
        IdentityType type = request.getTargetType();
        String target = IdentitySupport.normalize(type, request.getTarget());
        IdentitySupport.validateFormat(type, target);

        VerificationScene scene = request.getScene();

        // 防枚举：重置密码时账号不存在也返回成功，但不实际发送
        if (scene == VerificationScene.RESET_PASSWORD
                && userAccountRepository.findByIdentity(type, target) == null) {
            log.info("[USER] 重置密码目标不存在，静默返回 scene={} type={} target={}",
                    scene, type, IdentitySupport.mask(type, target));
            return VerificationCodeResponse.builder()
                    .sent(false)
                    .expireSeconds(properties.getTtlSeconds())
                    .build();
        }

        // 注册场景：目标已被绑定则直接拒绝
        if (scene == VerificationScene.REGISTER
                && userAccountRepository.existsIdentity(type, target)) {
            throw new IdentityAlreadyBoundException(type);
        }

        // 频控
        if (!codeStore.tryAcquireSendQuota(type, target, properties.getResendIntervalSeconds())) {
            throw new VerificationCodeTooFrequentException();
        }

        String code = generateCode();
        codeStore.save(scene, type, target, code, properties.getTtlSeconds());
        codeSender.send(type, target, code, scene);

        log.info("[USER] 验证码已发送 scene={} type={} target={}",
                scene, type, IdentitySupport.mask(type, target));

        return VerificationCodeResponse.builder()
                .sent(true)
                .debugCode(properties.isExposeCode() ? code : null)
                .expireSeconds(properties.getTtlSeconds())
                .build();
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
