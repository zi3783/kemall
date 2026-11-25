package com.kemall.iam.user.application.service;

import com.kemall.iam.api.dto.user.RegisterRequest;
import com.kemall.iam.api.dto.user.RegisterResponse;
import com.kemall.iam.api.dto.user.ResetPasswordRequest;
import com.kemall.iam.api.enums.AccountType;
import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;
import com.kemall.iam.auth.application.port.PasswordEncoder;
import com.kemall.iam.auth.application.port.SessionStore;
import com.kemall.iam.user.application.config.VerificationProperties;
import com.kemall.iam.user.application.port.VerificationCodeStore;
import com.kemall.iam.user.application.support.IdentitySupport;
import com.kemall.iam.user.domain.exception.AccountNotFoundException;
import com.kemall.iam.user.domain.exception.IdentityAlreadyBoundException;
import com.kemall.iam.user.domain.exception.PasswordSameAsOldException;
import com.kemall.iam.user.domain.exception.UnsupportedAccountTypeException;
import com.kemall.iam.user.domain.exception.UsernameAlreadyExistsException;
import com.kemall.iam.user.domain.exception.VerificationCodeInvalidException;
import com.kemall.iam.user.domain.model.UserAccount;
import com.kemall.iam.user.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 账号应用服务：注册、密码重置。
 * <p>
 * 依赖 {@link SessionStore}（auth 子域应用端口）用于密码重置后强制所有设备下线。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountAppService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionStore sessionStore;
    private final VerificationCodeStore codeStore;
    private final VerificationProperties properties;

    /**
     * 账号注册：用户名 + 密码 + 手机号 + 验证码。
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String mobile = IdentitySupport.normalize(IdentityType.MOBILE, request.getMobile());

        String nickname = StringUtils.hasText(request.getNickname())
                ? request.getNickname().trim() : username;

        // 前置唯一性校验
        if (userAccountRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException();
        }
        if (userAccountRepository.existsIdentity(IdentityType.MOBILE, mobile)) {
            throw new IdentityAlreadyBoundException(IdentityType.MOBILE);
        }

        // 校验短信验证码
        requireCode(VerificationScene.REGISTER, IdentityType.MOBILE, mobile, request.getVerificationCode());

        UserAccount account = UserAccount.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(nickname)
                .status(1)
                .createdAt(LocalDateTime.now())
                .build();

        Long userId = userAccountRepository.create(account);
        userAccountRepository.bindIdentity(userId, IdentityType.MOBILE, mobile, LocalDateTime.now());

        // 消费验证码
        codeStore.consume(VerificationScene.REGISTER, IdentityType.MOBILE, mobile);

        log.info("[USER] 注册成功 userId={} username={}", userId, username);

        return RegisterResponse.builder()
                .userId(userId)
                .username(username)
                .build();
    }

    /**
     * 密码重置：手机/邮箱 + 验证码。
     * 重置成功后撤销该用户全部会话。
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        AccountType accountType = request.getAccountType();
        if (accountType != AccountType.MOBILE && accountType != AccountType.EMAIL) {
            throw new UnsupportedAccountTypeException();
        }
        IdentityType type = IdentityType.valueOf(accountType.name());
        String account = IdentitySupport.normalize(type, request.getAccount());
        IdentitySupport.validateFormat(type, account);

        requireCode(VerificationScene.RESET_PASSWORD, type, account, request.getVerificationCode());

        UserAccount user = userAccountRepository.findByIdentity(type, account);
        if (user == null) {
            throw new AccountNotFoundException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new PasswordSameAsOldException();
        }

        userAccountRepository.updatePasswordHash(user.getId(), passwordEncoder.encode(request.getNewPassword()));

        // 强制所有设备重新登录
        sessionStore.revokeAllSessions(user.getId());

        codeStore.consume(VerificationScene.RESET_PASSWORD, type, account);

        log.info("[USER] 密码重置成功 userId={}", user.getId());
    }

    private void requireCode(VerificationScene scene, IdentityType type, String target, String code) {
        if (!codeStore.verify(scene, type, target, code, properties.getMaxAttempts())) {
            throw new VerificationCodeInvalidException();
        }
    }
}
