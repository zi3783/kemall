package com.kemall.iam.user.application.service;

import com.kemall.iam.api.dto.user.BindIdentityRequest;
import com.kemall.iam.api.dto.user.CurrentUserInfo;
import com.kemall.iam.api.dto.user.IdentityInfo;
import com.kemall.iam.api.dto.user.PublicUserProfile;
import com.kemall.iam.api.dto.user.UnbindIdentityRequest;
import com.kemall.iam.api.dto.user.UpdateProfileRequest;
import com.kemall.iam.api.dto.user.UserProfileInfo;
import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.api.enums.VerificationScene;
import com.kemall.iam.user.application.config.VerificationProperties;
import com.kemall.iam.user.application.port.VerificationCodeStore;
import com.kemall.iam.user.application.support.IdentitySupport;
import com.kemall.iam.user.domain.exception.AccountNotFoundException;
import com.kemall.iam.user.domain.exception.IdentityAlreadyBoundException;
import com.kemall.iam.user.domain.exception.IdentityNotBoundException;
import com.kemall.iam.user.domain.exception.LastIdentityCannotUnbindException;
import com.kemall.iam.user.domain.exception.ProfileUpdateInvalidException;
import com.kemall.iam.user.domain.exception.VerificationCodeInvalidException;
import com.kemall.iam.user.domain.model.UserAccount;
import com.kemall.iam.user.domain.model.UserIdentityRef;
import com.kemall.iam.user.domain.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户资料应用服务：本人信息、资料查询/修改、手机号/邮箱绑定与解绑。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileAppService {

    private final UserAccountRepository userAccountRepository;
    private final VerificationCodeStore codeStore;
    private final VerificationProperties properties;

    /**
     * 当前登录用户完整信息（含脱敏后的绑定身份）。
     */
    public CurrentUserInfo getCurrentUser(Long userId) {
        UserAccount user = requireUser(userId);
        return CurrentUserInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .identities(toIdentityInfos(userId))
                .build();
    }

    /**
     * 本人资料。
     */
    public UserProfileInfo getMyProfile(Long userId) {
        UserAccount user = requireUser(userId);
        return UserProfileInfo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 他人公开资料（不含用户名、状态、绑定信息）。
     */
    public PublicUserProfile getPublicProfile(Long targetUserId) {
        UserAccount user = requireUser(targetUserId);
        return PublicUserProfile.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    /**
     * 修改昵称/头像，只更新非空字段。
     */
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        requireUser(userId);

        String nickname = request.getNickname() == null ? null : request.getNickname().trim();
        String avatarUrl = request.getAvatarUrl() == null ? null : request.getAvatarUrl().trim();

        if (nickname != null && nickname.isEmpty()) {
            throw new ProfileUpdateInvalidException();
        }
        if (avatarUrl != null && avatarUrl.isEmpty()) {
            throw new ProfileUpdateInvalidException();
        }
        if (!StringUtils.hasText(nickname) && !StringUtils.hasText(avatarUrl)) {
            throw new ProfileUpdateInvalidException();
        }

        userAccountRepository.updateProfile(userId, nickname, avatarUrl);
        log.info("[USER] 资料更新成功 userId={}", userId);
    }

    /**
     * 绑定手机号/邮箱：校验格式 → 唯一性 → 验证码 → 写入。
     */
    @Transactional
    public IdentityInfo bindIdentity(Long userId, BindIdentityRequest request) {
        requireUser(userId);

        IdentityType type = request.getIdentityType();
        String value = IdentitySupport.normalize(type, request.getIdentityValue());
        IdentitySupport.validateFormat(type, value);

        if (userAccountRepository.existsIdentity(type, value)) {
            throw new IdentityAlreadyBoundException(type);
        }

        requireCode(VerificationScene.BIND_IDENTITY, type, value, request.getVerificationCode());

        LocalDateTime now = LocalDateTime.now();
        userAccountRepository.bindIdentity(userId, type, value, now);
        codeStore.consume(VerificationScene.BIND_IDENTITY, type, value);

        log.info("[USER] 绑定身份成功 userId={} type={} value={}",
                userId, type, IdentitySupport.mask(type, value));

        return IdentityInfo.builder()
                .identityType(type)
                .maskedValue(IdentitySupport.mask(type, value))
                .verifiedAt(now)
                .build();
    }

    /**
     * 解绑手机号/邮箱：至少保留一种已验证身份，解绑前校验验证码。
     */
    @Transactional
    public void unbindIdentity(Long userId, IdentityType identityType, UnbindIdentityRequest request) {
        requireUser(userId);

        UserIdentityRef target = userAccountRepository.listIdentities(userId).stream()
                .filter(ref -> ref.getIdentityType() == identityType)
                .findFirst()
                .orElseThrow(IdentityNotBoundException::new);

        if (userAccountRepository.countVerifiedIdentities(userId) <= 1) {
            throw new LastIdentityCannotUnbindException();
        }

        requireCode(VerificationScene.UNBIND_IDENTITY, identityType,
                target.getIdentityValue(), request.getVerificationCode());

        userAccountRepository.unbindIdentity(userId, identityType);
        codeStore.consume(VerificationScene.UNBIND_IDENTITY, identityType, target.getIdentityValue());

        log.info("[USER] 解绑身份成功 userId={} type={}", userId, identityType);
    }

    private UserAccount requireUser(Long userId) {
        UserAccount user = userAccountRepository.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException();
        }
        return user;
    }

    private List<IdentityInfo> toIdentityInfos(Long userId) {
        return userAccountRepository.listIdentities(userId).stream()
                .map(ref -> IdentityInfo.builder()
                        .identityType(ref.getIdentityType())
                        .maskedValue(IdentitySupport.mask(ref.getIdentityType(), ref.getIdentityValue()))
                        .verifiedAt(ref.getVerifiedAt())
                        .build())
                .toList();
    }

    private void requireCode(VerificationScene scene, IdentityType type, String target, String code) {
        if (!codeStore.verify(scene, type, target, code, properties.getMaxAttempts())) {
            throw new VerificationCodeInvalidException();
        }
    }
}
