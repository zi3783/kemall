package com.kemall.iam.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.kemall.iam.api.enums.IdentityType;
import com.kemall.iam.user.domain.model.UserAccount;
import com.kemall.iam.user.domain.model.UserIdentityRef;
import com.kemall.iam.user.domain.repository.UserAccountRepository;
import com.kemall.iam.user.infrastructure.persistence.entity.UserAccountPO;
import com.kemall.iam.user.infrastructure.persistence.entity.UserIdentityPO;
import com.kemall.iam.user.infrastructure.persistence.mapper.UserAccountMapper;
import com.kemall.iam.user.infrastructure.persistence.mapper.UserIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户账号仓储实现（DDD Anti-Corruption Layer）。
 */
@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final UserAccountMapper userAccountMapper;
    private final UserIdentityMapper userIdentityMapper;

    @Override
    public UserAccount findById(Long userId) {
        return toDomain(userAccountMapper.selectById(userId));
    }

    @Override
    public UserAccount findByIdentity(IdentityType identityType, String identityValue) {
        return toDomain(userAccountMapper.selectByIdentity(identityType.name(), identityValue));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userAccountMapper.existsByUsername(username);
    }

    @Override
    public boolean existsIdentity(IdentityType identityType, String identityValue) {
        return userIdentityMapper.existsIdentity(identityType.name(), identityValue);
    }

    @Override
    public Long create(UserAccount account) {
        UserAccountPO po = new UserAccountPO();
        po.setId(IdWorker.getId());
        po.setUsername(account.getUsername());
        po.setPasswordHash(account.getPasswordHash());
        po.setNickname(account.getNickname());
        po.setAvatarUrl(account.getAvatarUrl());
        po.setStatus(account.getStatus());
        LocalDateTime now = account.getCreatedAt() == null ? LocalDateTime.now() : account.getCreatedAt();
        po.setCreatedAt(now);
        po.setUpdatedAt(now);
        userAccountMapper.insert(po);
        return po.getId();
    }

    @Override
    public void updateProfile(Long userId, String nickname, String avatarUrl) {
        userAccountMapper.updateProfile(userId, nickname, avatarUrl);
    }

    @Override
    public void updatePasswordHash(Long userId, String newPasswordHash) {
        userAccountMapper.updatePasswordHash(userId, newPasswordHash);
    }

    @Override
    public List<UserIdentityRef> listIdentities(Long userId) {
        return userIdentityMapper.selectByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void bindIdentity(Long userId, IdentityType identityType, String identityValue, LocalDateTime verifiedAt) {
        UserIdentityPO po = new UserIdentityPO();
        po.setId(IdWorker.getId());
        po.setUserId(userId);
        po.setIdentityType(identityType.name());
        po.setIdentityValue(identityValue);
        po.setVerifiedAt(verifiedAt);
        po.setCreatedAt(LocalDateTime.now());
        userIdentityMapper.insert(po);
    }

    @Override
    public void unbindIdentity(Long userId, IdentityType identityType) {
        userIdentityMapper.deleteByIdentityType(userId, identityType.name());
    }

    @Override
    public int countVerifiedIdentities(Long userId) {
        return userIdentityMapper.countVerifiedByUserId(userId);
    }

    private UserAccount toDomain(UserAccountPO po) {
        if (po == null) return null;
        return UserAccount.builder()
                .id(po.getId())
                .username(po.getUsername())
                .passwordHash(po.getPasswordHash())
                .nickname(po.getNickname())
                .avatarUrl(po.getAvatarUrl())
                .status(po.getStatus())
                .lastLoginAt(po.getLastLoginAt())
                .createdAt(po.getCreatedAt())
                .build();
    }

    private UserIdentityRef toDomain(UserIdentityPO po) {
        return UserIdentityRef.builder()
                .identityType(IdentityType.valueOf(po.getIdentityType()))
                .identityValue(po.getIdentityValue())
                .verifiedAt(po.getVerifiedAt())
                .build();
    }
}
